package pt.isel

enum class Status {
    PENDING,
    ACCEPTED,
    REJECTED,
    ;

    fun toDatabaseValue(): String =
        when (this) {
            PENDING -> "pending"
            ACCEPTED -> "accepted"
            REJECTED -> "rejected"
        }

    companion object {
        fun fromDatabase(value: String): Status =
            when (value) {
                "pending" -> PENDING
                "accepted" -> ACCEPTED
                "rejected" -> REJECTED
                else -> throw IllegalArgumentException("Unknown status: $value")
            }
    }
}
