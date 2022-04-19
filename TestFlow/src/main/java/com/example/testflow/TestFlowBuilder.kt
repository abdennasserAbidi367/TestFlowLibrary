package com.example.testflow

import android.util.Log
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TestFlowBuilder {

    private val flow = (1..5).asFlow()
        /**
         * If you launch a coroutine using launch(Dispatchers.Main) while already on the main thread,
         * will the code execute immediately? No
         * */
        .asLiveData(Dispatchers.Main)
    val r = mutableListOf(0,1,0,2,3,0,4)

    fun collectingFlow() {
        val u = flowOf(r)

        flow {
            for (i in 0 until 20) {
                emit(i)
            }
            //emit(1)
        }/*.asLiveData(Dispatchers.Main)
            .observe(this) {
                Log.i("tessssst", "alaaaaaaa222: $it")
            }*/

        Log.i("tessssst", "alaaaaaaa: feagegagaegaga")
        /*flow.observe(this) {
            Log.i("tessssst", "alaaaaaaa: $it")
        }*/


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
    }
}