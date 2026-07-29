#!/usr/bin/env python3
"""Verify that pull requests run the same quality gates as pushes."""

from __future__ import annotations

import re
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
PULL_REQUEST_WORKFLOW = ROOT / ".github" / "workflows" / "pull_request.yml"
PUSH_WORKFLOW = ROOT / ".github" / "workflows" / "on_push.yml"


def yaml_block(text: str, key: str, indent: int) -> str:
    """Return a simple indentation-delimited YAML mapping block."""
    lines = text.splitlines()
    header = f"{' ' * indent}{key}:"

    try:
        start = lines.index(header)
    except ValueError as error:
        raise AssertionError(f"Missing YAML block: {header}") from error

    end = len(lines)
    for index in range(start + 1, len(lines)):
        line = lines[index]
        if not line.strip() or line.lstrip().startswith("#"):
            continue
        line_indent = len(line) - len(line.lstrip())
        if line_indent <= indent:
            end = index
            break

    return "\n".join(lines[start:end])


def gradle_quality_commands(job: str) -> set[str]:
    commands = set()
    for line in job.splitlines():
        match = re.match(r"\s*run\s*:\s*(.+?)\s*$", line)
        if not match:
            continue
        command = match.group(1)
        if command == "./gradlew check" or "testDebugUnitTest" in command:
            commands.add(command)
    return commands


def main() -> int:
    pull_request = PULL_REQUEST_WORKFLOW.read_text(encoding="utf-8")
    push = PUSH_WORKFLOW.read_text(encoding="utf-8")
    failures: list[str] = []

    def require(condition: bool, message: str) -> None:
        if not condition:
            failures.append(message)

    require("pull_request_target:" not in pull_request, "PR CI must not use pull_request_target")

    try:
        permissions = yaml_block(pull_request, "permissions", 0)
    except AssertionError as error:
        failures.append(str(error))
        permissions = ""

    require("contents: read" in permissions, "PR CI must explicitly grant only contents: read")
    require(": write" not in permissions, "PR CI must not grant write permissions")

    try:
        quality = yaml_block(pull_request, "quality", 2)
    except AssertionError as error:
        failures.append(str(error))
        quality = ""

    try:
        release = yaml_block(pull_request, "testing", 2)
    except AssertionError as error:
        failures.append(str(error))
        release = ""

    try:
        push_build = yaml_block(push, "build", 2)
    except AssertionError as error:
        failures.append(str(error))
        push_build = ""

    require("runs-on: ubuntu-latest" in quality, "PR quality gates must run on Linux")
    require("timeout-minutes: 30" in quality, "PR quality job must have a bounded timeout")
    require("uses: actions/checkout@v6" in quality, "PR quality job must check out the repository")
    require("persist-credentials: false" in quality, "PR quality checkout must not persist credentials")
    require("uses: actions/setup-java@v5" in quality, "PR quality job must install the supported JDK")
    require("java-version: '21'" in quality, "PR quality job must use JDK 21")
    require("uses: gradle/actions/setup-gradle@v5" in quality, "PR quality job must configure Gradle caching")
    require(
        "python3 .github/scripts/verify_pull_request_ci.py" in quality,
        "PR quality job must enforce this workflow policy",
    )

    require("${{ secrets." not in quality, "PR quality job must not reference repository secrets")
    require("decrypt-secrets" not in quality, "PR quality job must not decrypt release secrets")
    require("ENCRYPT_KEY" not in quality, "PR quality job must not request the release encryption key")

    push_quality_commands = gradle_quality_commands(push_build)
    pull_request_quality_commands = gradle_quality_commands(quality)
    require(
        push_quality_commands == pull_request_quality_commands,
        "PR quality Gradle commands must exactly match push quality commands "
        f"(push={sorted(push_quality_commands)}, PR={sorted(pull_request_quality_commands)})",
    )
    require(
        push_quality_commands
        == {
            "./gradlew check",
            "./gradlew testDebugUnitTest -Proborazzi.test.verify=true",
        },
        "Push quality command detection changed; review parity policy before updating it",
    )

    require("runs-on: macos-latest" in release, "Existing release-build lane must remain on macOS")
    require(
        "./gradlew app:bundleRelease --stacktrace" in release,
        "Existing PR release bundle gate must be preserved",
    )

    if failures:
        print("Pull request CI policy violations:", file=sys.stderr)
        for failure in failures:
            print(f"- {failure}", file=sys.stderr)
        return 1

    print("Pull request CI policy verified")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
