package de.darkatra.vrising.discord

import org.dizitart.no2.objects.Id

data class Schema(
    @Id
    val appVersion: String,
) {

    fun appVersionAsSemanticVersion(): SemanticVersion {
        val appVersion = this.appVersion.removePrefix("V").split(".")
        return SemanticVersion(
            major = appVersion[0].toInt(),
            minor = appVersion[1].toInt(),
            patch = appVersion[2].toInt()
        )
    }
}

data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
)
