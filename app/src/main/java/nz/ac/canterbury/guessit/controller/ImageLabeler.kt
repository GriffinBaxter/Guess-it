package nz.ac.canterbury.guessit.controller

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import nz.ac.canterbury.guessit.MainActivity
import okhttp3.internal.wait
import java.io.IOException

class ImageLabeler(var activity: Activity) {

    suspend fun setPhotoDescription(bitmap: Bitmap): String {
        //Load Image
        val image: InputImage
        try {
            image = InputImage.fromBitmap(bitmap, 0)
        } catch (e: IOException) {
            return "Failed to get Description of Image: Could not load Image"
        }

        //Label Image
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.6f)
            .build()
        val labeler = ImageLabeling.getClient(options)

        var str = "EMPTY"
        try {
            val result = labeler.process(image).await()
            str = getLabels(result)
        } catch (e: Exception) {
            Log.e("ImageLabeler", e.toString())
        }

        return str
    }

    fun getLabels(labels: MutableList<ImageLabel>): String {
        val labelTexts = mutableListOf<String>()
        var returnString = ""
        for (label in labels) {
            val text = label.text
            val confidence = label.confidence
            val index = label.index
            returnString += "${text}, "
            labelTexts.add(text)
            labelTexts.add(confidence.toString())
        }
        returnString.dropLast(2)
        Toast.makeText(activity as MainActivity, returnString, Toast.LENGTH_LONG).show()
        return returnString
    }
}