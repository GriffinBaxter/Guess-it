package nz.ac.canterbury.guessit.controller

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
//import nz.ac.canterbury.guessit.ui.nearby.NearbyFragment
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NearbyConnectionManager @Inject constructor(appContext: Context) {

    /**
     * Our handle to the [Nearby Connections API][ConnectionsClient].
     */
    var connectionsClient: ConnectionsClient

    init {
        connectionsClient = Nearby.getConnectionsClient(appContext)
    }

    /**
     * Strategy for telling the Nearby Connections API how we want to discover and connect to
     * other nearby devices. A star shaped strategy means we want to discover multiple devices but
     * only connect to and communicate with one at a time.
     */
    private val STRATEGY = Strategy.P2P_STAR

    /*
    The following variables are for tracking our own data
    */
    var myCodeName: String = CodenameGenerator.generate()

    private val packagename: String = "guessit.canterbury.ac.nz"

    var opponentName: String? = null

    private var opponentEndpointId: String? = null

    fun sendPayload(payloadString: String) {
        connectionsClient.sendPayload(
            opponentEndpointId!!,
            Payload.fromBytes(payloadString.toByteArray(Charsets.UTF_8))
        )
    }

    fun disconnectFromEndpoint() {
        opponentEndpointId?.let { connectionsClient.disconnectFromEndpoint(it) }
    }

    fun startAdvertising() {
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        // Note: Advertising may fail. To keep this demo simple, we don't handle failures.
        connectionsClient.startAdvertising(
            myCodeName,
            packagename, // Just use package name
            connectionLifecycleCallback,
            options
        )
    }

    // Callbacks for finding other devices
    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            connectionsClient.requestConnection(myCodeName, endpointId, connectionLifecycleCallback)
        }

        override fun onEndpointLost(endpointId: String) {
        }
    }

    fun startDiscovery(){
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(packagename, endpointDiscoveryCallback, options)
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
                handleConnectionResult?.invoke()
            }
        }

        override fun onDisconnected(endpointId: String) {
//            resetGame()
        }
    }

    var handlePayload: ((string: String) -> Unit)? = null

    var handleConnectionResult: (() -> Unit)? = null

    /** callback for receiving payloads */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            payload.asBytes()?.let {
                val value = String(it, Charsets.UTF_8)
                Log.e("PAYLOADRECEIVED", "VALUE: ${value}")
                handlePayload?.invoke(value)
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Determines the winner and updates game state/UI after both players have chosen.
            // Feel free to refactor and extract this code into a different method
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                Log.e("PAYLOAD", "PAYLOADTRANSFERSUCCESS")
            }
        }
    }

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

}