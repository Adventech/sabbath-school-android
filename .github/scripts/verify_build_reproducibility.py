#!/usr/bin/env python3
"""Fail when Android build inputs can drift without an explicit review."""

from __future__ import annotations

import hashlib
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
WORKFLOWS = ROOT / ".github" / "workflows"
WRAPPER_PROPERTIES = ROOT / "gradle" / "wrapper" / "gradle-wrapper.properties"
WRAPPER_JAR = ROOT / "gradle" / "wrapper" / "gradle-wrapper.jar"
VERIFICATION_METADATA = ROOT / "gradle" / "verification-metadata.xml"
VERSION_CATALOG = ROOT / "gradle" / "libs.versions.toml"
JAVA_VERSION_FILE = ROOT / ".java-version"

# Values copied from Gradle's release checksum reference. Updating the wrapper
# intentionally requires reviewing both the distribution and wrapper JAR.
APPROVED_GRADLE_CHECKSUMS = {
    "9.4.0": {
        "distribution": "60ea723356d81263e8002fec0fcf9e2b0eee0c0850c7a3d7ab0a63f2ccc601f3",
        "wrapper_jar": "55243ef57851f12b070ad14f7f5bb8302daceeebc5bce5ece5fa6edb23e1145c",
    },
}

EXACT_JAVA_VERSION = re.compile(r"^\d+\.\d+\.\d+\+\d+$")
EXACT_DEPENDENCY_VERSION = re.compile(
    r"^\d+(?:\.\d+)+(?:[-+][0-9A-Za-z][0-9A-Za-z.-]*)?$"
)
DYNAMIC_VERSION = re.compile(
    r"(?:SNAPSHOT|latest(?:\.|$)|(?:^|\.)\+$|^[\[(]|[\])]$)", re.IGNORECASE
)


def parse_properties(path: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        key, separator, value = line.partition("=")
        if separator:
            properties[key.strip()] = value.strip()
    return properties


def check_workflows(errors: list[str]) -> None:
    setup_java_count = 0
    java_version_file_count = 0
    gradle_workflow_count = 0

    for path in sorted((*WORKFLOWS.glob("*.yml"), *WORKFLOWS.glob("*.yaml"))):
        text = path.read_text(encoding="utf-8")
        relative = path.relative_to(ROOT)

        for runner in re.findall(r"^\s*runs-on:\s*['\"]?([^'\"\s#]+)", text, re.MULTILINE):
            if runner.endswith("-latest"):
                errors.append(f"{relative}: mutable runner label {runner!r}")

        setup_java_count += len(re.findall(r"uses:\s*actions/setup-java@", text))
        java_version_file_count += len(
            re.findall(r"java-version-file:\s*['\"]?\.java-version['\"]?", text)
        )
        if re.search(r"^\s*java-version:", text, re.MULTILINE):
            errors.append(f"{relative}: use .java-version instead of an inline Java range")

        if "./gradlew" in text:
            gradle_workflow_count += 1
            if "uses: actions/setup-java@" not in text:
                errors.append(f"{relative}: Gradle runs without actions/setup-java")
            if not re.search(
                r"java-version-file:\s*['\"]?\.java-version['\"]?", text
            ):
                errors.append(f"{relative}: Gradle does not use .java-version")

    if setup_java_count == 0:
        errors.append("no actions/setup-java step found")
    elif setup_java_count != java_version_file_count:
        errors.append(
            "every actions/setup-java step must use java-version-file: .java-version "
            f"({setup_java_count} setup steps, {java_version_file_count} version-file inputs)"
        )
    if gradle_workflow_count == 0:
        errors.append("no active Gradle workflow found")


def check_java_version(errors: list[str]) -> None:
    if not JAVA_VERSION_FILE.is_file():
        errors.append(".java-version is missing")
        return

    java_version = JAVA_VERSION_FILE.read_text(encoding="utf-8").strip()
    if not EXACT_JAVA_VERSION.fullmatch(java_version):
        errors.append(
            ".java-version must pin an exact feature/interim/update/build version "
            f"(found {java_version!r})"
        )
        return

    catalog_text = VERSION_CATALOG.read_text(encoding="utf-8")
    match = re.search(r'^jdk\s*=\s*"(?P<major>\d+)"\s*$', catalog_text, re.MULTILINE)
    if not match:
        errors.append("gradle/libs.versions.toml must declare the Java language version")
    elif java_version.split(".", 1)[0] != match.group("major"):
        errors.append(
            f".java-version ({java_version}) disagrees with the Gradle Java language "
            f"version ({match.group('major')})"
        )


def check_wrapper(errors: list[str]) -> None:
    properties = parse_properties(WRAPPER_PROPERTIES)
    distribution_url = properties.get("distributionUrl", "")
    match = re.fullmatch(
        r"https\\://services\.gradle\.org/distributions/"
        r"gradle-(?P<version>\d+\.\d+\.\d+)-bin\.zip",
        distribution_url,
    )
    if not match:
        errors.append(f"unsupported or non-exact Gradle distribution URL: {distribution_url!r}")
        return

    version = match.group("version")
    approved = APPROVED_GRADLE_CHECKSUMS.get(version)
    if approved is None:
        errors.append(f"Gradle {version} has no reviewed checksum entry")
        return

    distribution_checksum = properties.get("distributionSha256Sum", "").lower()
    if distribution_checksum != approved["distribution"]:
        errors.append(
            f"Gradle {version} distribution checksum is missing or not the reviewed value"
        )

    wrapper_checksum = hashlib.sha256(WRAPPER_JAR.read_bytes()).hexdigest()
    if wrapper_checksum != approved["wrapper_jar"]:
        errors.append(
            f"Gradle {version} wrapper JAR checksum is not the reviewed value "
            f"({wrapper_checksum})"
        )


def check_dependency_versions(errors: list[str]) -> None:
    text = VERSION_CATALOG.read_text(encoding="utf-8")
    versions_match = re.search(r"(?ms)^\[versions\]\s*(.*?)(?=^\[)", text)
    if not versions_match:
        errors.append("gradle/libs.versions.toml has no [versions] table")
        return

    for line in versions_match.group(1).splitlines():
        match = re.fullmatch(r'\s*([A-Za-z0-9_.-]+)\s*=\s*"([^"]+)"\s*', line)
        if not match:
            continue
        name, value = match.groups()
        if name in {"jdk", "jvmTarget"}:
            continue
        if DYNAMIC_VERSION.search(value) or not EXACT_DEPENDENCY_VERSION.fullmatch(value):
            errors.append(f"gradle/libs.versions.toml: {name} is not exact ({value!r})")

    dependency_call = re.compile(
        r"(?:api|implementation|compileOnly|runtimeOnly|"
        r"testImplementation|androidTestImplementation|ksp|classpath)"
        r"\s*\(\s*['\"][^:'\"]+:[^:'\"]+:([^'\"]+)['\"]"
    )
    for path in sorted((*ROOT.rglob("*.gradle"), *ROOT.rglob("*.gradle.kts"))):
        if not path.is_file() or "build" in path.parts or ".gradle" in path.parts:
            continue
        for version in dependency_call.findall(path.read_text(encoding="utf-8")):
            if DYNAMIC_VERSION.search(version):
                errors.append(
                    f"{path.relative_to(ROOT)}: dynamic dependency selector {version!r}"
                )


def check_verification_metadata(errors: list[str]) -> None:
    if not VERIFICATION_METADATA.is_file():
        errors.append("gradle/verification-metadata.xml is missing")
        return

    try:
        root = ET.parse(VERIFICATION_METADATA).getroot()
    except ET.ParseError as error:
        errors.append(f"gradle/verification-metadata.xml is invalid XML: {error}")
        return

    namespace_match = re.match(r"\{([^}]+)\}", root.tag)
    namespace = {"v": namespace_match.group(1)} if namespace_match else {}
    prefix = "v:" if namespace else ""

    verify_metadata = root.findtext(f"{prefix}configuration/{prefix}verify-metadata", namespaces=namespace)
    if verify_metadata != "true":
        errors.append("dependency verification must verify Gradle metadata")

    components = root.findall(f"{prefix}components/{prefix}component", namespace)
    if not components:
        errors.append("dependency verification metadata contains no components")

    artifacts = root.findall(
        f"{prefix}components/{prefix}component/{prefix}artifact", namespace
    )
    missing_sha256 = [
        artifact.get("name", "<unnamed>")
        for artifact in artifacts
        if artifact.find(f"{prefix}sha256", namespace) is None
    ]
    if missing_sha256:
        preview = ", ".join(missing_sha256[:5])
        errors.append(
            f"{len(missing_sha256)} verified artifacts lack SHA-256 checksums"
            f" (first: {preview})"
        )


def main() -> int:
    errors: list[str] = []
    check_workflows(errors)
    check_java_version(errors)
    check_wrapper(errors)
    check_dependency_versions(errors)
    check_verification_metadata(errors)

    if errors:
        print("Build reproducibility contract failed:", file=sys.stderr)
        for error in errors:
            print(f"  - {error}", file=sys.stderr)
        return 1

    print("Build reproducibility contract passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
