package nz.ac.canterbury.guessit.ui.singlePhoto

import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.CallSuper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nz.ac.canterbury.guessit.controller.ImageLabeler
import nz.ac.canterbury.guessit.MainActivity
import nz.ac.canterbury.guessit.R
import nz.ac.canterbury.guessit.controller.NearbyConnectionManager
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class SinglePhotoFragment : Fragment() {

    @Inject
    lateinit var nearbyConnectionManager: NearbyConnectionManager

    lateinit var imageLabeler: ImageLabeler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_single_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nearbyConnectionManager.handlePayload = handlePayload

        imageLabeler = ImageLabeler(activity as MainActivity)

        val photoPath = arguments?.getString("photoPath")!!
        val singlePhotoImageView: ImageView = view.findViewById(R.id.singlePhotoView)
        Picasso.get().load("file:$photoPath").into(singlePhotoImageView)

        val latitude = arguments?.getString("latitude")!!.toDouble()
        val longitude = arguments?.getString("longitude")!!.toDouble()

        lifecycleScope.launch {
            val photoDescription = imageLabeler.getPhotoDescription(BitmapFactory.decodeFile(photoPath))
            Log.e("PHOTODESC", "Here: ${photoDescription}")
            val payload = JSONObject()
            payload.put("latitude", latitude)
            payload.put("longitude", longitude)
            payload.put("photoDescription", photoDescription)
            val payloadString = payload.toString()
            nearbyConnectionManager.sendPayload(payloadString)
        }
    }

    private val handlePayload: (string: String) -> Unit = { payload ->
        if (payload == "continue") {
            Navigation.findNavController(requireView()).navigate(R.id.action_singlePhotoFragment_to_showPhoto)
            sendReadyNotification()
        } else {
            Log.e("INVALID", "Invalid payload received")
        }
    }

    @CallSuper
    override fun onStop(){
        nearbyConnectionManager.resetHandlePayload()
        super.onStop()
    }

    fun sendReadyNotification(){
        val notification = NotificationCompat.Builder(requireContext(), "PlayerReadyReminder")
            .setSmallIcon(R.drawable.app_icon_foreground)
            .setContentTitle("Player Ready to Guess")
            .setContentText("The other player has finished guessing and has asked for another photo!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(
                PendingIntent.getActivity(
                    requireContext(),
                    0,
                    Intent(requireContext(), MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            .build()

        with(NotificationManagerCompat.from(requireContext())) {
            // notificationId is a unique int for each notification that you must define
            notify(1, notification)
        }
    }
}
