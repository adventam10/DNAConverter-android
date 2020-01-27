package am10.dnaconverter.models

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.app.ActivityCompat
import java.util.ArrayList

class SpeechModel(context: Context) {
    companion object{
        const val REQUEST_PERMISSION = 1100
    }
    var isRecording = false
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(context)

    fun isAcceptedRecordAudioPermission(context: Context) : Boolean {
        return ActivityCompat.checkSelfPermission(context,
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    }

    fun shouldShowRequestRecordAudioPermission(activity: Activity) : Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)
    }

    fun requestRecordAudioPermission(activity: Activity) {
        val reqPermissions = ArrayList<String>()
        reqPermissions.add(Manifest.permission.RECORD_AUDIO)
        ActivityCompat.requestPermissions(activity,
            reqPermissions.toTypedArray(), REQUEST_PERMISSION
        )
    }

    fun startListening(context: Context, callback: ((String) -> (Unit))?) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onBeginningOfSpeech() {
            }

            override fun onBufferReceived(buffer: ByteArray?) {
            }

            override fun onEndOfSpeech() {
            }

            override fun onError(error: Int) {
            }

            override fun onEvent(eventType: Int, params: Bundle) {
            }

            override fun onPartialResults(partialResults: Bundle) {
                val result = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                callback?.invoke(result!![0])
            }

            override fun onReadyForSpeech(params: Bundle?) {
            }

            override fun onResults(results: Bundle) {
                val result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                callback?.invoke(result!![0])
            }

            override fun onRmsChanged(rmsdB: Float) {
            }
        })
        isRecording = true
        recognizer.startListening(intent)
    }

    fun stopListening() {
        recognizer.stopListening()
        isRecording = false
    }
}