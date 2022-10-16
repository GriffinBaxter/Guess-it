package nz.ac.canterbury.guessit.ui.map

import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point

class MapState: ViewModel() {
    var centerPoint: Point = Point.fromLngLat(172.81749201636148, -42.21613418067329)
    var zoom = 4.0
    var bearing = 0.0
    var pitch = 0.0
    var score: Int? = null

    lateinit var selectedPoint: Point

    fun selectedPointInitialized(): Boolean {
        return this::selectedPoint.isInitialized
    }

}