package nz.ac.canterbury.guessit.ui.map

import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point

class MapState: ViewModel() {
    var centerPoint: Point = Point.fromLngLat(172.604180, -43.303350)
    var zoom = 7.0
    var bearing = 0.0
    var pitch = 0.0
    var score: Int? = null

    lateinit var selectedPoint: Point

    fun selectedPointInitialized(): Boolean {
        return this::selectedPoint.isInitialized
    }

}