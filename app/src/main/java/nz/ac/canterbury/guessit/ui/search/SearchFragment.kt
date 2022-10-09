package nz.ac.canterbury.guessit.ui.search

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint
import nz.ac.canterbury.guessit.R
import nz.ac.canterbury.guessit.controller.NearbyConnectionManager
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    @Inject
    lateinit var nearbyConnectionManager: NearbyConnectionManager

    lateinit var opponentNameTextView: TextView

    lateinit var statusTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nearbyConnectionManager.handlePayload = handlePayload
        nearbyConnectionManager.handleConnectionResult = handleConnectionResult
//        // TODO: Update this to modern check
//        if (ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                REQUEST_CODE_REQUIRED_PERMISSIONS
//            )
//        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }
        val myNameTextView: TextView = view.findViewById(R.id.myName)
        myNameTextView.text = "You\n(${nearbyConnectionManager.myCodeName})"

        opponentNameTextView = view.findViewById(R.id.opponentName)

        statusTextView = view.findViewById(R.id.status)
        val disconnectButton: Button = view.findViewById(R.id.disconnect)
        val findOpponentButton: Button = view.findViewById(R.id.findOpponent)
        findOpponentButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            nearbyConnectionManager.startAdvertising()
            nearbyConnectionManager.startDiscovery()
            statusTextView.text = "Searching for opponents..."
            // "find opponents" is the opposite of "disconnect" so they don't both need to be
            // visible at the same time
            findOpponentButton.visibility = View.GONE
            disconnectButton.visibility = View.VISIBLE
        }

        disconnectButton.setOnClickListener {
            nearbyConnectionManager.disconnectFromEndpoint()
        }

        val searchType = arguments?.getString("searchType")!!

        val startGameButton: Button = view.findViewById(R.id.startGame)
        startGameButton.setOnClickListener {
            Log.e("STARTGAME", "Game starting as ${searchType}")
            if (searchType == "host") {
                Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_showPhoto)
            } else {
                Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_waitFragment)
            }
        }
    }

    private val handlePayload: (string: String) -> Unit = {
        Log.e("TEST", "TESTFUNCTION_SEARCHFRAGMENT")
        Log.e("PAYLOADHANDLE", "PAYLOAD: ${it}")
    }

    private val handleConnectionResult: () -> Unit = {
        opponentNameTextView.text = nearbyConnectionManager.opponentName
        statusTextView.text = "Connected"
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //granted
            Log.e("PERMISSIONS", "BT granted")
        }else{
            //deny
            Log.e("PERMISSIONS", "BT denied")
        }
    }

    @CallSuper
    override fun onStop(){
        nearbyConnectionManager.connectionsClient.apply {
            stopAdvertising()
            stopDiscovery()
            // TODO: Probably want to only do this when the new connection manager is destroyed, or explicitly.
//            stopAllEndpoints()
        }
//        resetGame()
        super.onStop()
    }

}