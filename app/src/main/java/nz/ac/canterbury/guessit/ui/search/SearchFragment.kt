package nz.ac.canterbury.guessit.ui.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
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

    private val hasNearbyPermissions
        @RequiresApi(Build.VERSION_CODES.S)
        get() = requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nearbyConnectionManager.handlePayload = handlePayload
        nearbyConnectionManager.handleConnectionResult = handleConnectionResult

        if (!hasNearbyPermissions) {
            requestPermissions(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), 1)
        } else {
            startSearch()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (
            permissions.contains(Manifest.permission.BLUETOOTH_SCAN) &&
            grantResults[permissions.indexOf(Manifest.permission.BLUETOOTH_SCAN)] == 0 &&
            permissions.contains(Manifest.permission.BLUETOOTH_CONNECT) &&
            grantResults[permissions.indexOf(Manifest.permission.BLUETOOTH_CONNECT)] == 0 &&
            permissions.contains(Manifest.permission.BLUETOOTH_ADVERTISE) &&
            grantResults[permissions.indexOf(Manifest.permission.BLUETOOTH_ADVERTISE)] == 0 &&
            permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
            grantResults[permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)] == 0
        ) {
            startSearch()
        } else {
            Toast.makeText(context, "The nearby permission and precise location are required to start the game.", Toast.LENGTH_LONG).show()
            Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_homeFragment)
        }
    }

    private fun startSearch() {
        nearbyConnectionManager.startAdvertising()
        nearbyConnectionManager.startDiscovery()

        val searchText: TextView = requireView().findViewById(R.id.searchText)
        val searchType = arguments?.getString("searchType")!!
        if (searchType == "host") {
            searchText.text = resources.getString(R.string.searching_for_player)
        } else if (searchType == "player") {
            searchText.text = resources.getString(R.string.searching_for_host)
        }
    }

    private val handlePayload: (string: String) -> Unit = {

    }

    private val handleConnectionResult: () -> Unit = {
        val searchType = arguments?.getString("searchType")!!
        if (searchType == "host") {
            Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_showPhoto)
        } else {
            Navigation.findNavController(requireView()).navigate(R.id.action_searchFragment_to_waitFragment)
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
