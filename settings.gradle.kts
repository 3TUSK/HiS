pluginManagement {
    repositories {
        // The main repo
        gradlePluginPortal() 
        // Used for GrGit. We can use JGit directly, but it is cumbersome.
        maven {
            name = "ajoberstar-backup"
            url = uri("https://ajoberstar.github.io/bintray-backup/")
        }
    }
}
