package core.schedule

class PriorityQueue<T : Comparable<T>> {
    private val items: MutableList<T> = mutableListOf()

    fun enqueue(item: T) {
        items.add(item)
        heapifyUp(size() - 1)
    }

    fun dequeue(): T? {
        if (isEmpty()) return null

        swap(0, size() - 1)
        val item = items.removeAt(size() - 1)
        heapifyDown(0)

        return item
    }

    fun peek(): T? {
        return if (isEmpty()) null else items[0]
    }

    fun isEmpty(): Boolean {
        return items.isEmpty()
    }

    fun size(): Int {
        return items.size
    }

    private fun heapifyUp(index: Int) {
        var currentIndex = index
        var parentIndex = (currentIndex - 1) / 2

        while (currentIndex > 0 && items[currentIndex] < items[parentIndex]) {
            swap(currentIndex, parentIndex)
            currentIndex = parentIndex
            parentIndex = (currentIndex - 1) / 2
        }
    }

    private fun heapifyDown(index: Int) {
        var currentIndex = index
        var childIndex = getSmallerChildIndex(currentIndex)

        while (childIndex != -1 && items[currentIndex] > items[childIndex]) {
            swap(currentIndex, childIndex)
            currentIndex = childIndex
            childIndex = getSmallerChildIndex(currentIndex)
        }
    }

    private fun getSmallerChildIndex(parentIndex: Int): Int {
        val leftChildIndex = parentIndex * 2 + 1
        val rightChildIndex = parentIndex * 2 + 2

        return when {
            leftChildIndex >= size() -> -1
            rightChildIndex >= size() -> leftChildIndex
            items[leftChildIndex] < items[rightChildIndex] -> leftChildIndex
            else -> rightChildIndex
        }
    }

    private fun swap(index1: Int, index2: Int) {
        val temp = items[index1]
        items[index1] = items[index2]
        items[index2] = temp
    }
}