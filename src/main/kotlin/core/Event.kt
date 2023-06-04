package core

open class Event(
    val delay: Number,
    val priority: EventPriority = EventPriority.NORMAL,
    val note: String? = null,
    initialCallback: ((Event) -> Unit)? = null
) {
    private val callbacks = if (initialCallback == null) mutableListOf() else mutableListOf(initialCallback)
    fun addCallback(callback: (Event) -> Unit) {
        callbacks += callback
    }

    fun executeCallbacks() {
        callbacks.forEach { it(this) }
    }
}