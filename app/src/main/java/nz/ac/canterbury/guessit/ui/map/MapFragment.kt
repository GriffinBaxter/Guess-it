package nz.ac.canterbury.guessit.ui.map


import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import nz.ac.canterbury.guessit.MainActivity
import nz.ac.canterbury.guessit.R
import nz.ac.canterbury.guessit.controller.NearbyConnectionManager
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.roundToInt

@AndroidEntryPoint
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
    lateinit var imagePoint: Point

    lateinit var circleAnnotationManager: CircleAnnotationManager
    lateinit var polylineAnnotationManager: PolylineAnnotationManager

    @Inject
    lateinit var nearbyConnectionManager: NearbyConnectionManager

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
        resultsTitle = view.findViewById(R.id.resultsTitle)
        distanceText = view.findViewById(R.id.distanceText)
        pointsEarned = view.findViewById(R.id.pointsEarnedText)
        continueButton = view.findViewById(R.id.map_continueButton)
        shareButton = view.findViewById(R.id.map_shareButton)
        map_guessButton = view.findViewById(R.id.map_guessButton)
        resultsLayout = view.findViewById(R.id.map_resultsLayout)
        mapView = view.findViewById(R.id.mapView)

        val newLat = arguments?.getString("latitude")!!
        val newLong = arguments?.getString("longitude")!!
        photoDescriptionTextView.text = arguments?.getString("photoDescription")!!
        imagePoint = Point.fromLngLat(newLong.toDouble(), newLat.toDouble())

        resultsLayout.visibility = View.INVISIBLE

        mapboxMap = mapView.getMapboxMap()

        // Create an instance of the Annotation API and get the CircleAnnotationManager.
        val annotationApi = mapView.annotations
        circleAnnotationManager = annotationApi.createCircleAnnotationManager()
        polylineAnnotationManager = annotationApi.createPolylineAnnotationManager()

        val preferences: SharedPreferences = androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
        val darkMode = preferences.getBoolean("darkMode", false)
        var style = Style.MAPBOX_STREETS
        if (darkMode) {
            style = Style.DARK
        }

        mapView.getMapboxMap().loadStyleUri(style)
        mapView.scalebar.isMetricUnits = true

        mapboxMap.addOnMapClickListener { point ->
            //Make it so you cant click the map if you arent guessing anymore
            if (resultsLayout.visibility == View.INVISIBLE) {
                selectedPoint = point

                addPoint()

                map_guessButton.isEnabled = true
            }
            true
        }

        map_guessButton.setOnClickListener {
            manageGuess()
        }

        continueButton.setOnClickListener {
            nearbyConnectionManager.sendPayload(getString(R.string.continuePayload))
            Navigation.findNavController(requireView()).navigate(R.id.action_mapFragment_to_waitFragment)
        }

        shareButton.setOnClickListener {
            shareScore(score!!)
        }

        setMapState()
    }

    fun setMapState() {
        val model: MapState by viewModels()

        val cameraOptions = CameraOptions.Builder()
            .center(model.centerPoint)
            .zoom(model.zoom)
            .bearing(model.bearing)
            .pitch(model.pitch)
            .build()

        mapboxMap.setCamera(cameraOptions)

        map_guessButton.isEnabled = false
        if (model.selectedPointInitialized()) {
            selectedPoint = model.selectedPoint
            addPoint()
            map_guessButton.isEnabled = true
        }

        //On the guessing screen
        if (model.score != null) {
            manageGuess()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        val model: MapState by viewModels()
        model.centerPoint = mapboxMap.cameraState.center
        model.zoom = mapboxMap.cameraState.zoom
        model.bearing = mapboxMap.cameraState.bearing
        model.pitch = mapboxMap.cameraState.pitch
        if (this::selectedPoint.isInitialized) model.selectedPoint = selectedPoint
        model.score = score
    }

    private fun manageGuess() {
        val distance = getDistance(imagePoint, selectedPoint)
        addLine()

        score = calculateScore(distance)

        map_guessButton.visibility = View.INVISIBLE
        resultsLayout.visibility = View.VISIBLE

        if (score!! > 4500) resultsTitle.text = getString(R.string.map_guess_close)
        else if (score!! > 2000) resultsTitle.text = getString(R.string.map_guess_goodJob)
        else resultsTitle.text = getString(R.string.map_guess_betterLuckNextTime)

        distanceText.text = getString(R.string.map_distanceFrom, distance)
        pointsEarned.text = getString(R.string.map_pointsEarned, score)

        moveMapOverLine()
        //Toast.makeText(requireContext(), "Latitude: ${selectedPoint.latitude()}\nLongitude: ${selectedPoint.longitude()}\nDistance: ${(distance * 100.0).roundToInt() / 100.0}km", Toast.LENGTH_SHORT).show()
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
        //Max score is 5000
        val score = 4999.91 * (0.998036).pow(distance)
        return score.roundToInt()
    }

    private fun shareScore(score: Int) {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.shareMessage, score))
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
        val width = displayMetrics.widthPixels


        // Create a polygon
        val triangleCoordinates = listOf(
            listOf(
                selectedPoint,
                imagePoint
            )
        )
        val polygon = Polygon.fromLngLats(triangleCoordinates)
// Convert to a camera options from a given geometry and padding

        val cameraPosition: CameraOptions

        if (inLandscape()) {
            cameraPosition = mapboxMap.cameraForGeometry(polygon, EdgeInsets(100.0, 100.0, 100.0, (width/2.0)))
        } else {
            cameraPosition = mapboxMap.cameraForGeometry(polygon, EdgeInsets(100.0, 100.0, (height/2.3), 100.0))
        }



// Set camera position
        mapboxMap.setCamera(cameraPosition)
    }

    private fun inLandscape(): Boolean {
        val orientation = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }
}