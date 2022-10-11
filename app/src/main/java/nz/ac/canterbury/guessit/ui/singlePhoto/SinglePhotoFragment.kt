package nz.ac.canterbury.guessit.ui.singlePhoto

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    var confirmMapFragmentReceived = false

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
            while (!confirmMapFragmentReceived) {
                nearbyConnectionManager.sendPayload(payloadString)
            }
        }
    }

    private val handlePayload: (string: String) -> Unit = { payload ->
        if (payload == "continue") {
//            Navigation.findNavController(requireView()).navigate(R.id.action_mapFragment_to_waitFragment)
        }
    }
}
