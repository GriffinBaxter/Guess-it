package nz.ac.canterbury.guessit


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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


class MapFragment : Fragment() {


    lateinit var map_guessButton: Button
    lateinit var mapView: MapView
    lateinit var mapboxMap: MapboxMap
    lateinit var selectedPoint: Point

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)




        mapView = view.findViewById(R.id.mapView)
        mapboxMap = mapView.getMapboxMap()
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        mapView.scalebar.isMetricUnits = true

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

            true
        }

        map_guessButton = view.findViewById(R.id.map_guessButton)
        map_guessButton.setOnClickListener {
            if (!this::selectedPoint.isInitialized) {
                Toast.makeText(activity as MainActivity, "Please select a point on the map to guess", Toast.LENGTH_SHORT).show()
            }
            else {
                manageGuess()
            }
        }


        return view
    }

    private fun manageGuess() {
        Toast.makeText(activity as MainActivity, "Latitude: ${selectedPoint.latitude()}\nLongitude: ${selectedPoint.longitude()}", Toast.LENGTH_SHORT).show()
    }



}