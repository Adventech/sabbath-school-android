import extensions.applyDefault

plugins.apply(BuildPlugins.UPDATE_DEPENDENCIES)

allprojects {
    repositories.applyDefault()

    plugins.apply(BuildPlugins.KTLINT)
}
