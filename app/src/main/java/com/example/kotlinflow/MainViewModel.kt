package com.example.kotlinflow

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainViewModel : ViewModel() {

    private val flow = (1..5).asFlow().asLiveData()

    private val s5 = flow {
        /**
         * never have to try-catch the emit() or emitAll() calls.
         * */
        try {
            emit("Something")
        } catch (ex: Exception) {
            emit("Error!")
        }
    }

    val q = (1..5).toMutableList()
    val s = flow {
        try {
            for (e in 6..10) {
                (q[e])
            }
        } catch (ex: Exception) {
            emit("Error! ${ex.message}")
        }
    }

    val s1 = flow<Int> {
        for (e in 6..10) {
            (q[e])
        }
    }

    val callBacks = callbackFlow {
        for (e in 6..10) {
            send(q[e])
        }
    }

    fun collectData() {
        viewModelScope.launch {
            callBacks.catch {
                Log.i("tessssst", "error: $it  ${flow.value}")
            }.collect {
                Log.i("tessssst", "final: $it")
            }
        }
    }
}