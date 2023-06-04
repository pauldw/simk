package core

import core.schedule.PriorityQueue
import core.schedule.ScheduleEntry

class Environment(initialTime: Double = 0.0) {
    private val heap = PriorityQueue<ScheduleEntry>()
    private val entryIdIterator: Iterator<Int> = generateSequence(0) { it + 1 }.iterator()
    private val processes = mutableListOf<Iterator<Event?>>()
    var now: Double = initialTime
        private set

    fun schedule(event: Event?) {
        if (event == null) return
        heap.enqueue(ScheduleEntry(now + event.delay.toDouble(), entryIdIterator.next(), event))
    }

    // TODO: this function name is confusing given the verb/noun confusion. Maybe rename to "addProcess"?
    fun process(sequence: Sequence<Event?>) {
        schedule(Event(0, EventPriority.URGENT) { processes += sequence.iterator() })
    }

    private fun step() {
        heap.dequeue()?.let { entry ->
            now = entry.entryTime
            // TODO make this a debug level logger call
            //println("core.Event \"${entry.event.note}\" at time ${entry.entryTime} with priority ${entry.event.priority}")
            entry.event.executeCallbacks()
        }

        // Get next event from any processes. Processes are expected to yield null if there is no NEW event.
        processes.forEach { process ->
            if (process.hasNext()) {
                schedule(process.next())
            }
        }
    }

    fun run(until: Number? = null) {
        do { step() } while (heap.peek() != null && (until == null || heap.peek()!!.entryTime < until.toDouble()))
    }
}