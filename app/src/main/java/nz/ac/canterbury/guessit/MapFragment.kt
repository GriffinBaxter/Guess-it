package nz.ac.canterbury.guessit

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.maps.MapView
import com.mapbox.maps.Style


class MapFragment : Fragment() {


    var mapView: MapView? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var locationSet = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view: View = inflater.inflate(R.layout.fragment_map, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as MainActivity)


        mapView = view.findViewById(R.id.mapView)
        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)




        return view
    }
//This aint working btw. you need permissions
    @SuppressLint("MissingPermission")
    private fun obtieneLocalizacion(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                var latitude = location?.latitude
                var longitude = location?.longitude
                Log.e("loc", latitude.toString() + "     " + longitude.toString())
                locationSet = true
            }
    }
}