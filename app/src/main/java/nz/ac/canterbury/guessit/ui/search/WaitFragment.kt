package nz.ac.canterbury.guessit.ui.search

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import dagger.hilt.android.AndroidEntryPoint
import nz.ac.canterbury.guessit.R
import nz.ac.canterbury.guessit.controller.NearbyConnectionManager
import javax.inject.Inject

@AndroidEntryPoint
class WaitFragment : Fragment() {

    @Inject
    lateinit var nearbyConnectionManager: NearbyConnectionManager
    lateinit var payloadDisplayText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wait, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nearbyConnectionManager.handlePayload = handlePayload

        val sendPayloadButton: Button = view.findViewById(R.id.sendPayloadButton)
        sendPayloadButton.setOnClickListener {
            Log.e("SENDINGPAYLOAD", "SENDING...")
            nearbyConnectionManager.sendPayload("WAITPAYLOAD")
        }

        payloadDisplayText = view.findViewById(R.id.receivedPayload)


    }

    private val handlePayload: (string: String) -> Unit = {
        Log.e("TEST", "TESTFUNCTION_SEARCHFRAGMENT")
        Log.e("PAYLOADHANDLE", "PAYLOAD: ${it}")
        payloadDisplayText.text = "Received: $it"
    }

}