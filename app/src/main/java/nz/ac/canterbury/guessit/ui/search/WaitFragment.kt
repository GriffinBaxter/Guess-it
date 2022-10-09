package nz.ac.canterbury.guessit.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import nz.ac.canterbury.guessit.R
import nz.ac.canterbury.guessit.controller.NearbyConnectionManager
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class WaitFragment : Fragment() {

    @Inject
    lateinit var nearbyConnectionManager: NearbyConnectionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wait, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nearbyConnectionManager.handlePayload = handlePayload
    }

    private val handlePayload: (string: String) -> Unit = { payload ->
        val jsonPayload = JSONObject(payload)
        if (jsonPayload.has("latitude") && jsonPayload.has("longitude") && jsonPayload.has("photoDescription")) {
            val latitude = jsonPayload.get("latitude").toString()
            val longitude = jsonPayload.get("longitude").toString()
            val photoDescription = jsonPayload.get("photoDescription").toString()
            val args = bundleOf(
                "latitude" to latitude, "longitude" to longitude, "photoDescription" to photoDescription
            )
            Navigation.findNavController(requireView()).navigate(R.id.action_waitFragment_to_mapFragment, args)
        } else {
            Log.e("Invalid Payload", "Invalid Payload Received in WaitFragment")
        }
    }

}