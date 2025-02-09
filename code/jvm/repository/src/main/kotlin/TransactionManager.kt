package pt.isel

/** Generic repository interface for managing transactions */
interface TransactionManager {
    /**
     * This method creates an instance of Transaction, potentially initializing a JDBI Handle, which
     * is then passed as an argument to the Transaction constructor.
     */
    fun <R> run(block: Transaction.() -> R): R
}
