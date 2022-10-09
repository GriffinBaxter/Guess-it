package nz.ac.canterbury.guessit.ui.map


import android.content.Intent
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.EdgeInsets
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
import kotlin.math.pow
import kotlin.math.roundToInt


class MapFragment : Fragment() {


    lateinit var photoDescriptionTextView: TextView
    lateinit var mapView: MapView
    lateinit var map_guessButton: Button
    lateinit var resultsLayout: LinearLayout

    lateinit var mapboxMap: MapboxMap
    lateinit var selectedPoint: Point

    lateinit var resultsTitle: TextView
    lateinit var distanceText: TextView
    lateinit var pointsEarned: TextView
    lateinit var continueButton: Button
    lateinit var shareButton: Button

    lateinit var imageLabeler: ImageLabeler

    lateinit var imagePoint: Point

    lateinit var circleAnnotationManager: CircleAnnotationManager
    lateinit var polylineAnnotationManager: PolylineAnnotationManager

    var score: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photoDescriptionTextView = view.findViewById(R.id.photoFeatures)

        val newLat = arguments?.getString("latitude")!!
        val newLong = arguments?.getString("longitude")!!
        photoDescriptionTextView.text = arguments?.getString("photoDescription")!!
        imagePoint = Point.fromLngLat(newLong.toDouble(), newLat.toDouble())

        imageLabeler = ImageLabeler(activity as MainActivity)

        resultsLayout = view.findViewById(R.id.map_resultsLayout)
        resultsLayout.visibility = View.INVISIBLE

        resultsTitle = view.findViewById(R.id.resultsTitle)
        distanceText = view.findViewById(R.id.distanceText)
        pointsEarned = view.findViewById(R.id.pointsEarnedText)
        continueButton = view.findViewById(R.id.map_continueButton)
        shareButton = view.findViewById(R.id.map_shareButton)


        mapView = view.findViewById(R.id.mapView)
        mapboxMap = mapView.getMapboxMap()
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        mapView.scalebar.isMetricUnits = true


        // Create an instance of the Annotation API and get the CircleAnnotationManager.
        val annotationApi = mapView.annotations
        circleAnnotationManager = annotationApi.createCircleAnnotationManager()
        polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()

        mapboxMap.addOnMapClickListener { point ->
            //Make it so you cant click the map if you arent guessing anymore
            if (resultsLayout.visibility == View.INVISIBLE) {
                selectedPoint = point

                addPoint()

                map_guessButton.isEnabled = true
            }
            true
        }

        map_guessButton = view.findViewById(R.id.map_guessButton)
        map_guessButton.setOnClickListener {
            manageGuess()
        }
        map_guessButton.isEnabled = false

        continueButton.setOnClickListener {
            //Do something here
        }

        shareButton.setOnClickListener {
            shareScore(score!!)
        }
    }

    private fun manageGuess() {
        val distance = getDistance(imagePoint, selectedPoint)
        addLine()

        score = calculateScore(distance)

        map_guessButton.visibility = View.INVISIBLE
        resultsLayout.visibility = View.VISIBLE

        if (score!! < 100) resultsTitle.text = getString(R.string.map_guess_close)
        if (score!! < 500) resultsTitle.text = getString(R.string.map_guess_goodJob)
        else resultsTitle.text = getString(R.string.map_guess_betterLuckNextTime)

        distanceText.text = getString(R.string.map_distanceFrom, distance)
        pointsEarned.text = getString(R.string.map_pointsEarned, score)

        moveMapOverLine()
        //Toast.makeText(activity as MainActivity, "Latitude: ${selectedPoint.latitude()}\nLongitude: ${selectedPoint.longitude()}\nDistance: ${(distance * 100.0).roundToInt() / 100.0}km", Toast.LENGTH_SHORT).show()
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

    private fun calculateScore(distance: Double): Int {
        val score = 4999.91 * (0.998036).pow(distance)
        return score.roundToInt()
    }

    private fun shareScore(score: Int) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "I just got a score of $score on GuessIt!")
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
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

    private fun moveMapOverLine() {
        val displayMetrics = DisplayMetrics()
        (activity as MainActivity).windowManager.defaultDisplay.getMetrics(displayMetrics)

        val height = displayMetrics.heightPixels


        // Create a polygon
        val triangleCoordinates = listOf(
            listOf(
                selectedPoint,
                imagePoint
            )
        )
        val polygon = Polygon.fromLngLats(triangleCoordinates)
// Convert to a camera options from a given geometry and padding

        val cameraPosition = mapboxMap.cameraForGeometry(polygon, EdgeInsets(100.0, 100.0, (height/2.3), 100.0))

// Set camera position
        mapboxMap.setCamera(cameraPosition)
    }
}