package de.darkatra.vrising.discord.migration

data class SemanticVersion(
    val major: Int,
    val minor: Int,
    val patch: Int
) {

    companion object {
        fun getComparator(): Comparator<SemanticVersion> {
            return Comparator.comparing(SemanticVersion::major)
                .thenComparing(SemanticVersion::minor)
                .thenComparing(SemanticVersion::patch)
        }
    }

    override fun toString(): String {
        return "$major.$minor.$patch"
    }
}
