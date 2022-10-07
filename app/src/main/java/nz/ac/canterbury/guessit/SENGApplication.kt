package nz.ac.canterbury.guessit

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import nz.ac.canterbury.guessit.database.PhotoDatabase
import nz.ac.canterbury.guessit.database.PhotoRepository

@HiltAndroidApp
class SENGApplication: Application() {
    val database by lazy { PhotoDatabase.getDatabase(this) }
    val repository by lazy { PhotoRepository(database.photoDao()) }
}
