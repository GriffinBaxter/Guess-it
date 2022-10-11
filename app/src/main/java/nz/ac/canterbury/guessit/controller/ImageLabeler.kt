package nz.ac.canterbury.guessit.controller

import android.app.Activity
import android.content.res.Resources
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import nz.ac.canterbury.guessit.MainActivity
import nz.ac.canterbury.guessit.R
import java.io.IOException

class ImageLabeler(var activity: Activity) {

    suspend fun getPhotoDescription(bitmap: Bitmap): String {
        //Load Image
        val image: InputImage
        try {
            image = InputImage.fromBitmap(bitmap, 0)
        } catch (e: IOException) {
            return Resources.getSystem().getString(R.string.failedToLoadImageDescription)
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
        var returnString = ""
        if (labels.size == 0) return returnString
        for (label in labels) {
            returnString += "${label.text}, "
        }
        returnString = returnString.substring(0, returnString.length - 2)
        Toast.makeText(activity as MainActivity, returnString, Toast.LENGTH_LONG).show()
        return returnString
    }
}