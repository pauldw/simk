// Based on https://github.com/Kotlin/coroutines-examples/blob/master/examples/generator/generator.kt

import core.Event
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

/*
   ES6-style generator that can send values between coroutine and outer code in both ways.

   Note that in ES6-generators the first invocation of `next()` goes not accept a parameter, but
   just starts a coroutine until a subsequent `yield`, so to adopt it for the type-safe interface
   we must declare `next` to be always invoked with a parameter and make our coroutine receive the first
   parameter to `next` when it starts (so it is not lost). We also have to introduce an additional parameter to
   `yieldAll` to start a delegated generator.
*/

class Interrupt : Exception()

interface Brocess<out T> {
    fun hasNext(): Boolean
    fun next(): T?
    fun interrupt()
}

@RestrictsSuspension
interface BrocessBuilder<in T> {
    suspend fun yield(value: T)
    suspend fun yield(brocess: Brocess<T>)
}

fun process(block: suspend BrocessBuilder<Event?>.() -> Unit): Brocess<Event?> = brocess(block)

fun <T> brocess(block: suspend BrocessBuilder<T>.() -> Unit): Brocess<T> {
    val coroutine = BrocessCoroutine<T>()
    val initial: suspend () -> Unit = { block(coroutine) }
    initial.startCoroutine(coroutine)
    return coroutine
}

// Generator coroutine implementation class
internal class BrocessCoroutine<T>: Brocess<T>, BrocessBuilder<T>, Continuation<Unit> {
    lateinit var nextStep: () -> Unit
    lateinit var interruptStep: (Throwable) -> Unit
    private var lastValue: T? = null
    private var lastException: Throwable? = null
    private var finished = false

    // Generator<T> implementation

    override fun next(): T? {
        if (!hasNext()) throw NoSuchElementException()
        nextStep()
        lastException?.let { throw it }
        return lastValue
    }

    override fun hasNext(): Boolean {
        return !finished
    }

    override fun interrupt() {
        interruptStep(Interrupt())
    }

    // GeneratorBuilder<T, R> implementation

    override suspend fun yield(value: T): Unit = suspendCoroutineUninterceptedOrReturn { cont ->
        lastValue = value
        nextStep = { cont.resume(Unit) }
        interruptStep = { cont.resumeWithException(it) }
        COROUTINE_SUSPENDED
    }

    override suspend fun yield(brocess: Brocess<T>): Unit = suspendCoroutineUninterceptedOrReturn sc@ { cont ->
        if(!brocess.hasNext()) return@sc Unit // delegated coroutine does not generate anything -- resume
        lastValue = brocess.next()
        nextStep = {
            if (brocess.hasNext()) {
                lastValue = brocess.next()
            } else {
                cont.resume(Unit)
            }
        }
        interruptStep = { throwable -> cont.resumeWithException(throwable) }
        COROUTINE_SUSPENDED
    }

    // Continuation<Unit> implementation

    override val context: CoroutineContext get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<Unit>) {
        result
            .onSuccess {
                lastValue = null
                finished = true
            }
            .onFailure {throwable ->
                lastException = throwable
            }
    }
}