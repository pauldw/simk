package core

import brocess
fun timeout(delay: Number, priority: EventPriority = EventPriority.NORMAL, note: String? = null) = brocess {
    var fired = false
    yield(Event(delay, priority, note) { fired = true })
    while (!fired) {
        yield(null)
    }
}