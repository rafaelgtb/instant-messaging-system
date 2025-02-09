package pt.isel

interface UpdatedMessageEmitter {
    fun emit(signal: UpdatedMessage)

    fun onCompletion(callback: () -> Unit)

    fun onError(callback: (Throwable) -> Unit)
}
