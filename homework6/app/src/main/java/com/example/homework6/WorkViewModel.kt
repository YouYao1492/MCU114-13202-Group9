package com.example.homework6

// 1. Import MediaPlayer and Application
import android.app.Application
import android.media.MediaPlayer
import android.os.Handler
import android.os.HandlerThread
import androidx.lifecycle.AndroidViewModel // 2. Change from ViewModel to AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
// import androidx.lifecycle.ViewModel // No longer needed

// 3. Update the class to extend AndroidViewModel
class WorkViewModel(private val app: Application) : AndroidViewModel(app) {
    private val handlerThread = HandlerThread("VM-Work").apply { start() }
    private val worker = Handler(handlerThread.looper)

    private val _progress = MutableLiveData(0)
    val progress: LiveData<Int> = _progress

    private val _status = MutableLiveData("Idle")
    val status: LiveData<String> = _status

    // 4. Add a variable for the MediaPlayer
    private var mediaPlayer: MediaPlayer? = null

    @Volatile private var running = false

    fun start() {
        if (running) return
        running = true
        _status.postValue("Preparing…")
        _progress.postValue(0)

        // 5. Start the music when the work begins
        startMusic()

        worker.post {
            try {
                Thread.sleep(10000) // discovery，準備工作放這裏
                _status.postValue("Working…")
                for (i in 1..100) {
                    if (!running) break
                    Thread.sleep(350) // 真正的背㬌工作放這裏
                    _progress.postValue(i)
                }
                _status.postValue(if (running) "背景工作結朿！" else "Canceled")
                running = false
                // 6. Stop the music when work is done
                stopMusic()
            } catch (_: InterruptedException) {
                _status.postValue("Canceled")
                running = false
                // Also stop music on interruption
                stopMusic()
            }
        }
    }

    fun cancel() {
        running = false
        // 7. Stop the music immediately on cancellation
        stopMusic()
    }

    override fun onCleared() {
        running = false
        // 8. Make sure music is stopped and resources are released when ViewModel is destroyed
        stopMusic()
        handlerThread.quitSafely()
        super.onCleared()
    }

    // --- 9. Add helper methods for MediaPlayer ---
    private fun startMusic() {
        // Ensure we don't create multiple players
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(app, R.raw.background_music)
            mediaPlayer?.isLooping = true // Loop the music
        }
        mediaPlayer?.start() // Start playing
    }

    private fun stopMusic() {
        // Use post to ensure MediaPlayer is accessed from the same thread if needed, or manage thread safety
        worker.post {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}
