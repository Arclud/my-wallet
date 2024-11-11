package kg.ivy.wallet.migrations

interface Migration {
    val key: String

    suspend fun migrate()
}
