package pt.isel

enum class AccessType {
    READ_ONLY,
    READ_WRITE,
    ;

    fun toDatabaseValue(): String =
        when (this) {
            READ_ONLY -> "read-only"
            READ_WRITE -> "read-write"
        }

    companion object {
        fun fromDatabase(value: String): AccessType =
            when (value) {
                "read-only" -> READ_ONLY
                "read-write" -> READ_WRITE
                else -> throw IllegalArgumentException("Unknown access type: $value")
            }
    }
}
