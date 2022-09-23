package nz.ac.canterbury.guessit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener


class MapFragment : Fragment() {


    lateinit var latLongLabel: TextView
    lateinit var mapView: MapView
    lateinit var mapboxMap: MapboxMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)


        latLongLabel = view.findViewById(R.id.latLongLabel)


        mapView = view.findViewById(R.id.mapView)
        mapboxMap = mapView.getMapboxMap()
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

        // Create an instance of the Annotation API and get the CircleAnnotationManager.
        val annotationApi = mapView.annotations
        val circleAnnotationManager = annotationApi.createCircleAnnotationManager()

        mapboxMap.addOnMapClickListener { point ->

            Toast.makeText(activity as MainActivity, String.format("User clicked at: %s", point.toString()), Toast.LENGTH_LONG).show()

            //Adding marker to map
            circleAnnotationManager.deleteAll()
            // Set options for the resulting circle layer.
            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(point)
                // Style the circle that will be added to the map.
                .withCircleRadius(8.0)
                .withCircleColor("#3399ff")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")
            // Add the resulting circle to the map.
            circleAnnotationManager.create(circleAnnotationOptions)


            latLongLabel.text = "Latitude: ${point.latitude()}\nLongitude: ${point.longitude()}"
            true
        }


        return view
    }




}