package core

// Convenient way to create a core.Timeout process
class Timeout(
    private val delay: Number,
    private val priority: EventPriority = EventPriority.NORMAL,
    val note: String? = null
) : Iterator<Event?> {
    private var fired = false
    private var alreadyReturnedEvent = false
    override fun next(): Event? {
        if (alreadyReturnedEvent) return null
        alreadyReturnedEvent = true
        return Event(delay, priority, note) { fired = true }
    }

    override fun hasNext(): Boolean {
        return !fired
    }
}