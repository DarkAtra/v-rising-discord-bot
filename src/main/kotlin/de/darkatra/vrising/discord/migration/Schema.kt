package de.darkatra.vrising.discord.migration

import org.dizitart.no2.repository.annotations.Id

private val VERSION_PATTERN = Regex("^V(\\d+)\\.(\\d+)\\.(\\d+)").toPattern()

data class Schema(
    @Id
    val appVersion: String,
) {

    fun asSemanticVersion(): SemanticVersion {

        val matcher = VERSION_PATTERN.matcher(appVersion)
        if (!matcher.find() || matcher.groupCount() != 3) {
            error("Could not parse version from appVersion '$appVersion'.")
        }

        val result = matcher.toMatchResult()
        return SemanticVersion(
            major = result.group(1).toInt(),
            minor = result.group(2).toInt(),
            patch = result.group(3).toInt()
        )
    }
}
