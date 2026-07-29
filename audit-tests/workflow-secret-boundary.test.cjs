const assert = require("node:assert/strict")
const fs = require("node:fs")
const path = require("node:path")
const test = require("node:test")

const repositoryRoot = path.resolve(__dirname, "..")
const readWorkflow = (name) =>
    fs.readFileSync(
        path.join(repositoryRoot, ".github", "workflows", name),
        "utf8",
    )

test("ordinary push CI does not decrypt or receive release secrets", () => {
    const source = readWorkflow("on_push.yml")

    assert.doesNotMatch(source, /decrypt-secrets\.sh/)
    assert.doesNotMatch(source, /ENCRYPT_KEY/)
    assert.doesNotMatch(source, /\$\{\{\s*secrets\./)
})

test("ordinary push CI retains its quality and screenshot gates", () => {
    const source = readWorkflow("on_push.yml")

    assert.match(source, /\.\/gradlew check/)
    assert.match(
        source,
        /\.\/gradlew testDebugUnitTest -Proborazzi\.test\.verify=true/,
    )
})

test("release material remains scoped to publishing workflows", () => {
    for (const workflow of ["on_publish.yml", "release.yml"]) {
        const source = readWorkflow(workflow)
        assert.match(source, /decrypt-secrets\.sh/, workflow)
        assert.match(source, /ENCRYPT_KEY:\s*\$\{\{ secrets\.ENCRYPT_KEY \}\}/, workflow)
    }
})
