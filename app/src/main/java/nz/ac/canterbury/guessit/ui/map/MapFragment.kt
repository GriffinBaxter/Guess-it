package nz.ac.canterbury.guessit.ui.map


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
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.scalebar.scalebar
import nz.ac.canterbury.guessit.MainActivity
import nz.ac.canterbury.guessit.R
import nz.ac.canterbury.guessit.controller.ImageLabeler
import kotlin.math.roundToInt


class MapFragment : Fragment() {


    lateinit var photoDescriptionTextView: TextView
    lateinit var mapView: MapView
    lateinit var map_guessButton: Button

    lateinit var mapboxMap: MapboxMap
    lateinit var selectedPoint: Point

    lateinit var imageLabeler: ImageLabeler

    var imagePoint = Point.fromLngLat(172.604180, -43.303350)

    lateinit var circleAnnotationManager: CircleAnnotationManager
    lateinit var polylineAnnotationManager: PolylineAnnotationManager

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
        circleAnnotationManager = annotationApi.createCircleAnnotationManager()
        polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()

        mapboxMap.addOnMapClickListener { point ->
            selectedPoint = point

            addPoint()

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
        val distance = getDistance(imagePoint, selectedPoint)
        addLine()

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


    private fun addPoint() {
        circleAnnotationManager.deleteAll()
        //Selected point
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
    }


    private fun addLine() {
        polylineAnnotationManager.deleteAll()
        // Set options for the resulting line layer.
        val polylineAnnotationOptions: PolylineAnnotationOptions = PolylineAnnotationOptions()
            .withPoints(arrayListOf(imagePoint, selectedPoint))
            // Style the line that will be added to the map.
            .withLineColor("#008080")
            .withLineWidth(5.0)
        //Make the line dashed
        polylineAnnotationManager.lineDasharray = listOf(2.0, 0.5)

        // Add the resulting line to the map.
        polylineAnnotationManager.create(polylineAnnotationOptions)

        //Image point
        // Set options for the resulting circle layer.
        val circleAnnotationOptions = CircleAnnotationOptions()
            // Define a geographic coordinate.
            .withPoint(imagePoint)
            // Style the circle that will be added to the map.
            .withCircleRadius(8.0)
            .withCircleColor("#FF0000")
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor("#ffffff")
        // Add the resulting circle to the map.
        circleAnnotationManager.create(circleAnnotationOptions)

    }
}