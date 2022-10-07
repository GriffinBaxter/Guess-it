package nz.ac.canterbury.guessit.Controller

import android.app.Activity
import android.graphics.Bitmap
import android.widget.Toast
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import nz.ac.canterbury.guessit.MainActivity
import java.io.IOException

class ImageLabeler(var activity: Activity) {

    fun setPhotoDescription(bitmap: Bitmap): String {
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
        labeler.process(image)
            .addOnSuccessListener { labels ->
                str = getLabels(labels)
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