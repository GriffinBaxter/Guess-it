package nz.ac.canterbury.guessit.ui.search

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.CallSuper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import nz.ac.canterbury.guessit.R
import nz.ac.canterbury.guessit.databinding.FragmentNearbyBinding
import nz.ac.canterbury.guessit.databinding.FragmentSearchBinding
import nz.ac.canterbury.guessit.ui.nearby.NearbyFragment
import java.util.*

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    /**
     * Our handle to the [Nearby Connections API][ConnectionsClient].
     */
    private lateinit var connectionsClient: ConnectionsClient

    private var myCodeName: String = SearchFragment.CodenameGenerator.generate()

    private val packagename: String = "guessit.canterbury.ac.nz"

    /**
     * Strategy for telling the Nearby Connections API how we want to discover and connect to
     * other nearby devices. A star shaped strategy means we want to discover multiple devices but
     * only connect to and communicate with one at a time.
     */
    private val STRATEGY = Strategy.P2P_STAR

    /*
    The following variables are convenient ways of tracking the data of the opponent that we
    choose to play against.
    */
    private var opponentName: String? = null
    private var opponentEndpointId: String? = null
    private var opponentScore = 0
//    private var opponentChoice: SearchFragment.GameChoice? = null

    /**
     * Instead of having each player enter a name, in this sample we will conveniently generate
     * random human readable names for players.
     */
    internal object CodenameGenerator {
        private val COLORS = arrayOf(
            "Red", "Orange", "Yellow", "Green", "Blue", "Indigo", "Violet", "Purple", "Lavender"
        )
        private val TREATS = arrayOf(
            "Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb",
            "Ice Cream Sandwich", "Jellybean", "Kit Kat", "Lollipop", "Marshmallow", "Nougat",
            "Oreo", "Pie"
        )
        private val generator = Random()

        /** Generate a random Android agent codename  */
        fun generate(): String {
            val color = COLORS[generator.nextInt(COLORS.size)]
            val treat = TREATS[generator.nextInt(TREATS.size)]
            return "$color $treat"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root
        connectionsClient = Nearby.getConnectionsClient(requireContext())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        binding.myName.text = "You\n($myCodeName)"
        binding.findOpponent.setOnClickListener {
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
            startAdvertising()
            startDiscovery()
            binding.status.text = "Searching for opponents..."
            // "find opponents" is the opposite of "disconnect" so they don't both need to be
            // visible at the same time
            binding.findOpponent.visibility = View.GONE
            binding.disconnect.visibility = View.VISIBLE
        }
        // wire the controller buttons
//        binding.apply {
//            rock.setOnClickListener { sendGameChoice(NearbyFragment.GameChoice.ROCK) }
//            paper.setOnClickListener { sendGameChoice(NearbyFragment.GameChoice.PAPER) }
//            scissors.setOnClickListener { sendGameChoice(NearbyFragment.GameChoice.SCISSORS) }
//        }
        binding.disconnect.setOnClickListener {
            opponentEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
//            resetGame()
        }

//        resetGame() // we are about to start a new game



//        val searchText: TextView = view.findViewById(R.id.searchText)
        val searchType = arguments?.getString("searchType")!!
//        if (searchType == "host") {
//            searchText.text = resources.getString(R.string.searching_for_player)
//        } else if (searchType == "player") {
//            searchText.text = resources.getString(R.string.searching_for_host)
//        }

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

    private fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
            myCodeName,
            packagename, // Just use package name
            connectionLifecycleCallback,
            options
        )
    }

    private fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packagename, endpointDiscoveryCallback, options)
    }

    // Callbacks for finding other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myCodeName, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
        }
    }

    // Callbacks for connections to other devices
    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
            // Accepting a connection means you want to receive messages. Hence, the API expects
            // that you attach a PayloadCall to the acceptance
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            opponentName = "Opponent\n(${info.endpointName})"
            Log.e("HERE", "onConnectionInitiated")
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            Log.e("HERE", "ONCONNECTIONRESULT")
            if (result.status.isSuccess) {
                Log.e("HERE", "ONCONNECTIONRESULT1")
                connectionsClient.stopAdvertising()
                connectionsClient.stopDiscovery()
                opponentEndpointId = endpointId
                binding.opponentName.text = opponentName
                binding.status.text = "Connected"
//                setGameControllerEnabled(true) // we can start playing
            }
        }

        override fun onDisconnected(endpointId: String) {
//            resetGame()
        }
    }

    /** callback for receiving payloads */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                Log.e("PAYLOAD", String(it, Charsets.UTF_8))
//                opponentChoice = NearbyFragment.GameChoice.valueOf(String(it, Charsets.UTF_8))
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Determines the winner and updates game state/UI after both players have chosen.
            // Feel free to refactor and extract this code into a different method
            if (update.status == PayloadTransferUpdate.Status.SUCCESS){
                Log.e("PAYLOAD", "TransferUpdate Success")
//                && myChoice != null && opponentChoice != null) {
//                val mc = myChoice!!
//                val oc = opponentChoice!!
//                when {
//                    mc.beats(oc) -> { // Win!
//                        binding.status.text = "${mc.name} beats ${oc.name}"
//                        myScore++
//                    }
//                    mc == oc -> { // Tie
//                        binding.status.text = "You both chose ${mc.name}"
//                    }
//                    else -> { // Loss
//                        binding.status.text = "${mc.name} loses to ${oc.name}"
//                        opponentScore++
//                    }
//                }
//                binding.score.text = "$myScore : $opponentScore"
//                myChoice = null
//                opponentChoice = null
//                setGameControllerEnabled(true)
            }
        }
    }

    @CallSuper
    override fun onStop(){
        connectionsClient.apply {
            stopAdvertising()
            stopDiscovery()
            stopAllEndpoints()
        }
//        resetGame()
        super.onStop()
    }

}