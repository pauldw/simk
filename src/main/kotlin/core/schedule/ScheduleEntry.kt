package core.schedule

import core.Event

data class ScheduleEntry(val entryTime: Double, val entryId: Int, val event: Event) :
    Comparable<ScheduleEntry> {
    override fun compareTo(other: ScheduleEntry): Int {
        return when {
            this.entryTime < other.entryTime -> -1
            this.entryTime > other.entryTime -> 1
            this.event.priority < other.event.priority -> -1
            this.event.priority > other.event.priority -> 1
            this.entryId < other.entryId -> -1
            this.entryId > other.entryId -> 1
            else -> 0
        }
    }
}