package com.example.homework5

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WorkViewModel : ViewModel() {

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _status = MutableLiveData("Preparing…")
    val status: LiveData<String> = _status

    private var running = false
    private var thread: Thread? = null

    fun start() {
        if (running) return
        running = true
        _status.postValue("Working…")
        thread = Thread {
            try {
                for (i in 0..100) {
                    if (!running || Thread.currentThread().isInterrupted) break

                    for (j in 0 until 10) {
                        Thread.sleep(10)
                        if (!running || Thread.currentThread().isInterrupted) break
                    }

                    _progress.postValue(i)
                    _status.postValue("Working… $i%")
                }
                if (running) {
                    _status.postValue("背景工作完成！！")
                }
            } catch (e: InterruptedException) {
                _status.postValue("Canceled")
            } finally {
                running = false
            }
        }
        thread?.start()
    }

    fun cancel() {
        running = false
        thread?.interrupt()           // 立即中斷 Thread
        _status.postValue("Canceled") // ProgressFragment 會即時顯示
        // 不重置 progress，保留最後值
    }
}

