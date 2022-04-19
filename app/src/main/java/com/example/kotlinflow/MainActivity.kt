package com.example.kotlinflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val flow = (1..5).asFlow()
        /**
         * If you launch a coroutine using launch(Dispatchers.Main) while already on the main thread,
         * will the code execute immediately? No
         * */
        .asLiveData(Dispatchers.Main)
    val r = mutableListOf(0,1,0,2,3,0,4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val u = flowOf(r)

        flow {
            for (i in 0 until 20) {
                emit(i)
            }
            //emit(1)
        }.asLiveData(Dispatchers.Main)
            .observe(this) {
                Log.i("tessssst", "alaaaaaaa222: $it")
            }

        Log.i("tessssst", "alaaaaaaa: feagegagaegaga")
        /*flow.observe(this) {
            Log.i("tessssst", "alaaaaaaa: $it")
        }*/

        mainViewModel.collectData()

        CoroutineScope(Dispatchers.Default).launch {
            flow<Int> {
                for (i in 0..20) {
                    emit(i)
                    delay(1000)
                }
            }.filter { i -> i < 10 }
                /*.fold(100) { acc, value ->
                    acc + value
                }*/
                /**
                 * The buffer function actually creates a second coroutine
                 * that allows the flow and collect functions to execute concurrently.
                 * The intermediate operators are not suspendable functions,
                 * but they can work with suspendable functions inside.(test() is suspend)*/
                //.buffer()
                .onEach {
                    Log.d("here", "$it")
                    test()
                }
                /**
                 * changes thread context(withContext)
                 * */
                .flowOn(Dispatchers.Default)
                .launchIn(CoroutineScope(Dispatchers.IO))
            /*.collect {
                delay(2000)
                Log.d("here", "$it")
            }*/
        }

        val flow2 = (1..5).asFlow()
        val zz = mutableListOf(1, 2, 3, 4, 5)
            .asFlow()
            .onEach {
                Log.d("here", "$it")
                test()
            }.launchIn(CoroutineScope(Dispatchers.Main))

        val flow1 = flow<Int> {
            emit(1)
            delay(1000)
            emit(2)
        }
        CoroutineScope(Dispatchers.Default).launch {
            flow1.flatMapConcat { value ->
                flow {
                    emit(value + 1)
                    delay(1000)
                    emit(value + 2)
                }
            }.collect {
                Log.i("collect", "The value is $it")
            }
        }

        /**
         * testing capacity: RENDEZVOUS
         * */
        val channel = Channel<Int>(capacity = RENDEZVOUS)
        CoroutineScope(Dispatchers.Default).launch {
            for (i in 0 until 20) {
                channel.send(i)
                Log.i("withoutConsumer", "onCreate: $i")
            }
        }

        /*CoroutineScope(Dispatchers.Default).launch {
            for (i in channel) {
                Log.i("withConsumer", "onCreate: $i")
            }
        }*/

        /**
         * comparing flow and RENDEZVOUS
         * */
        CoroutineScope(Dispatchers.Default).launch {
            val list = (1..100)
            flow<Int> {
                list.map {
                    emit(it)
                    Log.i("flowWithoutConsumer", "onCreate: $it")
                }
            }
            //add a consumer to begin sending data like RENDEZVOUS
            /*.collect {

            }*/
        }


        //Deferred values provide a convenient way to transfer a single value between coroutines.
        // Channels provide a way to transfer a stream of values.

        /**
         * Cold Streams
         * in the cold streams, all the data is produced inside the stream.
         * has one and only one subscriber and only emits values to that subscriber.
         * we can't have multiple instances of the same stream and they’re fully independent.
         * lazy stream: the emission of values starts only when someone subscribes to it.
         *  In other words, it begins when someone says “hey, start emitting that I’m here listening”.
         * As a result, cold data streams (like Sequence, Stream or Flow):
         *  can be infinite;
         *  do a minimal number of operations;
         *  use less memory (no need to allocate all the intermediate collections).
         * Hot Streams
         * all the data is produced outside the stream.it comes from external components.
         * A hot stream can have zero or more subscribers, and emits values to all of them at the same time.
         *  So, we can say that we have a multicast mechanism here.
         * eager stream: the emission of values always starts, no matter if there is someone subscribed or not.
         *  In other words, someone says “let’s listen to the stream emissions”.
         * */


        /**
         * channel vs flow
         * Flows can start emitting values no matter if there is someone listening to them or not.
         *
         * */

        /**
         * CallbackFlow vs flow
         * Flows can start emitting values no matter if there is someone listening to them or not.
         *
         * */

    }

    /**
     * Channels are conceptually similar to reactive streams.
     * It is a simple abstraction that you can use to transfer a stream of values between coroutines.
     *
     * params : capacity = maximum number of elements that a channel can contain in a buffer
     * RENDEZVOUS : the producer channel won’t produce anything until there is a consumer channel that needs data;
     * essentially, there is no buffer.
     * An element is transferred from producer to consumer only when the producer’s send and consumer’s receive invocations meet in time (rendezvous).
     * Because of this, the send function suspends until another coroutine invokes receive and receive suspends until another coroutine invokes send.
     * This is the reason for the RENDEZVOUS name.
     * channels the data is produced outside of the stream
     *
     *
     * explanation : when the channel reaches its N capacity,
     * the producer suspends until a consumer starts to read data from the same channel
     *
     *
     * you need to synchronize different coroutines with possible different execution contexts and scopes.
     * */
/*    fun <E> Channel(capacity: Int = RENDEZVOUS): Channel<E> {

    }*/

    //cold! This means, that a flow only emits data when it is collected (or consumed)
    suspend fun test() {

    }
}