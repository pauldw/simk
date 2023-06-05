import core.*

/*
First
Second
Third
Fourth
Fifth
Sixth
 */
fun priorityDemo() {
    val env = Environment()
    env.schedule(Event(1, EventPriority.LOW) { println("Fourth") })
    env.schedule(Event(2) { println("Fifth") })
    env.schedule(Event(10) { println("Sixth") })
    env.schedule(Event(0) { println("First") })
    env.schedule(Event(1) { println("Third") })
    env.schedule(Event(1, EventPriority.HIGH) { println("Second") })
    env.run()
}

/*
fast 0.0
slow 0.0
fast 0.5
slow 1.0
fast 1.0
fast 1.5
 */
fun clockDemo() {
    val env = Environment()

    fun clock(name: String, tick: Number) = process {
        while (true) {
            println("$name ${env.now}")
            yield(timeout(tick))
        }
    }

    env.addProcess(clock( "fast", 0.5))
    env.addProcess(clock("slow", 1))

    env.run(until=2)
}

/*
Start parking and charging at 0.0
Start driving at 5.0
Start parking and charging at 7.0
Start driving at 12.0
Start parking and charging at 14.0
 */
fun waitingForAProcess() {
    val env = Environment()
    class Car {
        fun run() = process {
            while (true) {
                println("Start parking and charging at ${env.now}")
                val chargeDuration = 5

                // We yield the process that process() returns to wait for it to finish
                yield(charge(chargeDuration))

                // The charge process has finished and we can start driving again.
                println("Start driving at ${env.now}")

                val tripDuration = 2
                yield(timeout(tripDuration, note = "trip complete"))
            }
        }

        fun charge(duration: Number) = process {
            yield(timeout(duration, note = "charge complete"))
        }
    }

    val car = Car()
    env.addProcess(car.run())
    env.run(until=15)
}

fun interruptingAnotherProcess() {
    val env = Environment()

    class Car {
        fun run() = process {
            while (true) {
                println("Start parking and charging at ${env.now}")
                val chargeDuration = 5

                // We yield the process that process() returns to wait for it to finish
                try {
                    yield(charge(chargeDuration))
                } catch(e: Interrupt) {
                    println("Was interrupted. Hope, the battery is full enough ...")
                }

                // The charge process has finished and we can start driving again.
                println("Start driving at ${env.now}")

                val tripDuration = 2
                yield(timeout(tripDuration, note = "trip complete"))
            }
        }

        fun charge(duration: Number) = process {
            yield(timeout(duration, note = "charge complete"))
        }
    }

    val car = Car()
    val carProcess = car.run()

    val driverProcess = brocess<Event?> {
        yield(timeout(3))
        carProcess.interrupt()
    }

    env.addProcess(carProcess)
    env.addProcess(driverProcess)
    env.run(until=15)

}
fun main() {
    //priorityDemo()
    clockDemo()
    //waitingForAProcess()
    // TODO interruptible processes and sends, maybe based on https://stackoverflow.com/questions/44214004/kotlin-alternative-to-pythons-coroutine-yield-and-send or a better understanding of https://github.com/Kotlin/KEEP/blob/master/proposals/coroutines.md
    //interruptingAnotherProcess()
}

