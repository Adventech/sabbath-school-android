import extensions.applyDefault

allprojects {
    repositories.applyDefault()

    plugins.apply(BuildPlugins.KTLINT)
}
