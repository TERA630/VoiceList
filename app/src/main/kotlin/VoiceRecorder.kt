package com.example.voicelist

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlin.concurrent.withLock

class VoiceRecorder(private val mCallback: Callback) {

    private val mSampleRateCandidates = intArrayOf(16000, 11025, 22050, 44100)
    private val mChannel = AudioFormat.CHANNEL_IN_MONO
    private val mEncoding = AudioFormat.ENCODING_PCM_16BIT

    private val mAmplitudeThreshold = 1500
    private val mSpeechTimeoutMs = 2000
    private val mMaxSpeechLengthMs = 30 * 1000
    private var mAudioRecord: AudioRecord? = null
    private var mThread: Thread? = null
    private lateinit var mBuffer: ByteArray
    private var mLock = java.util.concurrent.locks.ReentrantLock()


    interface Callback {
        fun onVoiceStart()  // called when the recorder starts hearing voice.
        fun onVoice(data: ByteArray, size: Int) { // called when the recorder is hearing voice.
            // @param data: The audio data in AudioFormat#ENCORDING_PCM_16BIT
            // @param size: The size of actual data in
        }
        fun onVoiceEnd() {} // called when the recorder stops hearing voice.
    }

    private var mLastVoiceHeardMillis = java.lang.Long.MAX_VALUE
    private var mVoiceStartedMillis: Long = 0

    fun start() {
        stop() // if it is current ongoing, stop it.
        mAudioRecord = createAudioRecord() ?: throw RuntimeException("Cannot instantiate VoiceRecorder")
        mAudioRecord?.let {
            Log.i("test", "voice recorder started..")
            it.startRecording()
            mThread = Thread(ProcessVoice())
            mThread!!.start()
        }
    }

    fun stop() {
        mLock.withLock {
            dismiss()
            mThread?.let {
                it.interrupt()
                mThread = null
            }
            mAudioRecord?.let {
                it.stop()
                it.release()
            }
            mAudioRecord = null
        }
    }

    fun dismiss() {
        if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
            mLastVoiceHeardMillis = Long.MAX_VALUE
            mCallback.onVoiceEnd()
        }
    }
    fun getSampleRate(): Int {
        return if (mAudioRecord != null) {
            val result = mAudioRecord?.sampleRate ?: 0
            result
        } else 0
    }

    private fun createAudioRecord(): AudioRecord? {

        for (sampleRate in mSampleRateCandidates) {
            val sizeInBytes = AudioRecord.getMinBufferSize(sampleRate, mChannel, mEncoding)
            if (sizeInBytes == AudioRecord.ERROR_BAD_VALUE) {
                continue
            }
            val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, mChannel, mEncoding, sizeInBytes)
            if (audioRecord.state == AudioRecord.STATE_INITIALIZED) {
                Log.i("audioRecord", "AudioRecord is initialized at $sampleRate,$sizeInBytes")
                mBuffer = ByteArray(sizeInBytes)
                return audioRecord
            } else {
                audioRecord.release()
            }
        }
        return null
    }
    // Continuously processes the captured audio and Notifies

    inner class ProcessVoice : Runnable {
        override fun run() {
            runLoop@ while (!Thread.currentThread().isInterrupted) {
                mLock.withLock {
                    mAudioRecord?.let {
                        val size = it.read(mBuffer, 0, mBuffer.size)
                        val now = System.currentTimeMillis()
                        if (isHearingVoice(mBuffer, size)) {
                            if (mLastVoiceHeardMillis == Long.MAX_VALUE) { // ボイスレコーダー開始時にmLast..はMAX_VALUEに､mVoiceStart..は今に
                                mVoiceStartedMillis = now
                                mCallback.onVoiceStart()
                            }
                            mCallback.onVoice(mBuffer, size)
                            mLastVoiceHeardMillis = now
                            if (now - mVoiceStartedMillis > mMaxSpeechLengthMs) end() // 経過30秒で終わり
                        } else if (mLastVoiceHeardMillis != Long.MAX_VALUE) {
                            mCallback.onVoice(mBuffer, size)
                            if (now - mVoiceStartedMillis > mSpeechTimeoutMs) end() // 無音は2秒でタイムアウト
                        }
                    }

                }
            }
        }

        private fun end() {
            mLastVoiceHeardMillis = Long.MAX_VALUE
            mCallback.onVoiceEnd()
        }
        private fun isHearingVoice(buffer: ByteArray, size: Int): Boolean {
            for (i in 0 until size - 1 step 2) {
                var s = buffer[i + 1].toInt() // Little endian  上位バイト
                if (s < 0) s *= -1 // 負数なら正数に
                s = s shl 8 // 上位バイト　
                s += Math.abs(buffer[i].toInt()) //　下位バイト
                if (s > mAmplitudeThreshold) return true
            }
            return false
        }
    }
}