package nz.ac.canterbury.guessit.UI.Fragment


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.scalebar.scalebar
import nz.ac.canterbury.guessit.Controller.ImageLabeler
import nz.ac.canterbury.guessit.MainActivity
import nz.ac.canterbury.guessit.R
import kotlin.math.roundToInt


class MapFragment : Fragment() {


    lateinit var photoDescriptionTextView: TextView
    lateinit var mapView: MapView
    lateinit var map_guessButton: Button

    lateinit var mapboxMap: MapboxMap
    lateinit var selectedPoint: Point

    lateinit var imageLabeler: ImageLabeler

    var testPoint = Point.fromLngLat(172.604180, -43.303350)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

        imageLabeler = ImageLabeler(activity as MainActivity)


        photoDescriptionTextView = view.findViewById(R.id.photoFeatures)
        mapView = view.findViewById(R.id.mapView)
        mapboxMap = mapView.getMapboxMap()
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        mapView.scalebar.isMetricUnits = true

        photoDescriptionTextView.text = "This is a cool photo!"

        // Create an instance of the Annotation API and get the CircleAnnotationManager.
        val annotationApi = mapView.annotations
        val circleAnnotationManager = annotationApi.createCircleAnnotationManager()

        mapboxMap.addOnMapClickListener { point ->
            selectedPoint = point

            //Adding marker to map
            circleAnnotationManager.deleteAll()
            // Set options for the resulting circle layer.
            val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
                // Define a geographic coordinate.
                .withPoint(selectedPoint)
                // Style the circle that will be added to the map.
                .withCircleRadius(8.0)
                .withCircleColor("#3399ff")
                .withCircleStrokeWidth(2.0)
                .withCircleStrokeColor("#ffffff")
            // Add the resulting circle to the map.
            circleAnnotationManager.create(circleAnnotationOptions)

            map_guessButton.isEnabled = true

            true
        }

        map_guessButton = view.findViewById(R.id.map_guessButton)
        map_guessButton.setOnClickListener {
            manageGuess()
        }
        map_guessButton.isEnabled = false

        return view
    }

    private fun manageGuess() {
        val distance = getDistance(testPoint, selectedPoint)

        Toast.makeText(activity as MainActivity, "Latitude: ${selectedPoint.latitude()}\nLongitude: ${selectedPoint.longitude()}\nDistance: ${(distance * 100.0).roundToInt() / 100.0}km", Toast.LENGTH_SHORT).show()
    }

    private fun getDistance(originalPoint: Point, guessedPoint: Point): Double {
        val theta = originalPoint.longitude() - guessedPoint.longitude()
        var dist =
                    Math.sin(deg2rad(originalPoint.latitude())) *
                    Math.sin(deg2rad(guessedPoint.latitude())) +
                    Math.cos(deg2rad(originalPoint.latitude())) *
                    Math.cos(deg2rad(guessedPoint.latitude())) *
                    Math.cos(deg2rad(theta))
        dist = Math.acos(dist)
        dist = rad2deg(dist)
        dist = dist * 60 * 1.853159616
        return dist
    }

    //This function converts decimal degrees to radians
    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    //This function converts radians to decimal degrees
    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
}