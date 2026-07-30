#!/usr/bin/env python3
"""Fail closed when the repository's GitHub Actions trust boundary regresses."""

from __future__ import annotations

from collections import Counter
from pathlib import Path
import re
import sys


ROOT = Path(__file__).resolve().parents[2]
WORKFLOW_DIR = ROOT / ".github" / "workflows"

# Each commit was resolved from the named upstream tag and its action manifest
# was reviewed at this exact commit before it was added here.
APPROVED_ACTIONS = {
    "actions/labeler": "b8dd2d9be0f68b860e7dae5dae7d772984eacd6d",  # v6
    "actions/checkout": "d23441a48e516b6c34aea4fa41551a30e30af803",  # v6
    "actions/setup-java": "03ad4de0992f5dab5e18fcb136590ce7c4a0ac95",  # v5
    "gradle/actions/setup-gradle": "0723195856401067f7a2779048b490ace7a47d7c",  # v5.0.2
    "actions/upload-release-asset": "e8f9f06c4b078e705bd2ea027f0926603fc9b4d5",  # v1.0.2
    "release-drafter/release-drafter": "eada3c96a64734dd381cfbda23511034e328ddb0",  # v7
    "r0adkll/upload-google-play": "935ef9c68bb393a8e6116b1575626a7f5be3a7fb",  # v1.1.3
}

EXPECTED_ACTION_COUNTS = Counter(
    {
        "actions/labeler": 1,
        "actions/checkout": 4,
        "actions/setup-java": 4,
        "gradle/actions/setup-gradle": 1,
        "actions/upload-release-asset": 1,
        "release-drafter/release-drafter": 1,
        "r0adkll/upload-google-play": 2,
    }
)

EXPECTED_WORKFLOWS = {
    "labels.yml",
    "on_publish.yml",
    "on_push.yml",
    "pull_request.yml",
    "release.yml",
}

EXPECTED_WRITES = {
    "labels.yml": Counter({"pull-requests": 1}),
    "on_publish.yml": Counter({"contents": 1}),
    "on_push.yml": Counter(),
    "pull_request.yml": Counter(),
    "release.yml": Counter({"contents": 1}),
}

USES_RE = re.compile(r"^\s*(?:-\s*)?uses:\s*([^\s#]+)")
FULL_SHA_RE = re.compile(r"^[0-9a-f]{40}$")
WRITE_RE = re.compile(r"^\s+([a-z-]+):\s*write\s*(?:#.*)?$")


def workflow_sources() -> dict[str, str]:
    paths = sorted((*WORKFLOW_DIR.glob("*.yml"), *WORKFLOW_DIR.glob("*.yaml")))
    return {path.name: path.read_text(encoding="utf-8").replace("\r\n", "\n") for path in paths}


def external_uses(text: str) -> list[tuple[int, str, str]]:
    uses: list[tuple[int, str, str]] = []
    for line_number, line in enumerate(text.splitlines(), 1):
        if line.lstrip().startswith("#"):
            continue
        match = USES_RE.match(line)
        if not match:
            continue
        reference = match.group(1)
        if reference.startswith("./") or reference.startswith("docker://"):
            continue
        if "@" not in reference:
            uses.append((line_number, reference, ""))
            continue
        action, revision = reference.rsplit("@", 1)
        uses.append((line_number, action, revision))
    return uses


def immutable_action_errors(sources: dict[str, str]) -> list[str]:
    errors: list[str] = []
    counts: Counter[str] = Counter()
    for name, source in sources.items():
        for line_number, action, revision in external_uses(source):
            counts[action] += 1
            approved = APPROVED_ACTIONS.get(action)
            if approved is None:
                errors.append(f"{name}:{line_number}: unreviewed external action {action}")
            elif not FULL_SHA_RE.fullmatch(revision):
                errors.append(f"{name}:{line_number}: mutable action ref {action}@{revision}")
            elif revision != approved:
                errors.append(f"{name}:{line_number}: unapproved commit for {action}")
    if counts != EXPECTED_ACTION_COUNTS:
        errors.append(f"external action inventory changed: expected {EXPECTED_ACTION_COUNTS}, got {counts}")
    return errors


def permission_errors(sources: dict[str, str]) -> list[str]:
    errors: list[str] = []
    top_level = re.compile(r"(?m)^permissions:\s*\n  contents:\s*read\s*$")
    for name, source in sources.items():
        if not top_level.search(source):
            errors.append(f"{name}: missing top-level contents: read permission")
        if re.search(r"(?m)^permissions:\s*(?:write-all|read-all)\s*$", source):
            errors.append(f"{name}: broad permission preset is forbidden")
        writes = Counter(
            match.group(1)
            for line in source.splitlines()
            if (match := WRITE_RE.match(line))
        )
        if writes != EXPECTED_WRITES[name]:
            errors.append(f"{name}: expected write grants {EXPECTED_WRITES[name]}, got {writes}")

    required_job_scopes = {
        "labels.yml": (
            "  label:\n"
            "    permissions:\n"
            "      contents: read\n"
            "      pull-requests: write"
        ),
        "on_publish.yml": "  build:\n    permissions:\n      contents: write",
        "release.yml": (
            "  update_draft_release:\n"
            "    permissions:\n"
            "      contents: write\n"
            "      pull-requests: read"
        ),
    }
    for name, snippet in required_job_scopes.items():
        if snippet not in sources[name]:
            errors.append(f"{name}: missing exact least-privilege job scope")
    return errors


def checkout_credential_errors(sources: dict[str, str]) -> list[str]:
    errors: list[str] = []
    for name, source in sources.items():
        lines = source.splitlines()
        for index, line in enumerate(lines):
            if "uses: actions/checkout@" not in line or line.lstrip().startswith("#"):
                continue
            indent = len(line) - len(line.lstrip())
            block: list[str] = []
            for following in lines[index + 1 :]:
                stripped = following.lstrip()
                following_indent = len(following) - len(stripped)
                if stripped.startswith("-") and following_indent < indent:
                    break
                block.append(following)
            if not any(re.match(r"^\s+persist-credentials:\s*false\s*$", item) for item in block):
                errors.append(f"{name}:{index + 1}: checkout credentials would persist")
    return errors


def release_concurrency_errors(sources: dict[str, str]) -> list[str]:
    errors: list[str] = []
    expected = {
        "on_publish.yml": (
            "concurrency:\n"
            "  group: android-release-assets\n"
            "  cancel-in-progress: false"
        ),
        "release.yml": (
            "concurrency:\n"
            "  group: android-google-play-internal\n"
            "  cancel-in-progress: false"
        ),
    }
    for name, snippet in expected.items():
        if snippet not in sources[name]:
            errors.append(f"{name}: missing non-cancelling release concurrency")
        if re.search(r"(?m)^\s*cancel-in-progress:\s*true\s*$", sources[name]):
            errors.append(f"{name}: a running release must never be cancelled")
    return errors


def release_boundary_errors(sources: dict[str, str]) -> list[str]:
    errors: list[str] = []
    publish = sources["on_publish.yml"]
    release = sources["release.yml"]
    if 'types: ["published"]' not in publish:
        errors.append("on_publish.yml: release asset upload is not limited to published releases")
    if "      - develop\n      - main" not in release:
        errors.append("release.yml: push trigger is not limited to develop/main")
    guard = "if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop'"
    if guard not in release:
        errors.append("release.yml: Play upload job lacks a defensive full-ref guard")
    return errors


def policy_execution_errors(sources: dict[str, str]) -> list[str]:
    errors: list[str] = []
    command = "run: python3 .github/scripts/validate_actions_policy.py"
    for name in ("on_push.yml", "pull_request.yml"):
        if command not in sources[name]:
            errors.append(f"{name}: policy regression script is not executed")
    return errors


def main() -> int:
    sources = workflow_sources()
    if set(sources) != EXPECTED_WORKFLOWS:
        print(
            f"workflow inventory changed: expected {sorted(EXPECTED_WORKFLOWS)}, "
            f"got {sorted(sources)}",
            file=sys.stderr,
        )
        return 1

    groups = {
        "immutable reviewed action refs": immutable_action_errors(sources),
        "explicit least token permissions": permission_errors(sources),
        "non-persistent checkout credentials": checkout_credential_errors(sources),
        "non-cancelling release concurrency": release_concurrency_errors(sources),
        "release event and ref boundaries": release_boundary_errors(sources),
        "policy executes in ordinary CI": policy_execution_errors(sources),
    }

    failures = 0
    for name, errors in groups.items():
        status = "PASS" if not errors else "FAIL"
        print(f"{status}: {name}")
        for error in errors:
            print(f"  - {error}")
        failures += bool(errors)

    print(f"tests {len(groups)}")
    print(f"pass {len(groups) - failures}")
    print(f"fail {failures}")
    return 1 if failures else 0


if __name__ == "__main__":
    raise SystemExit(main())
