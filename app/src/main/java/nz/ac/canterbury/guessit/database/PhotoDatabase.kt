package nz.ac.canterbury.guessit.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Photo::class],
    version = 1,
    // Good practice to enable this once our schema is finalised.
    exportSchema = false
)
abstract class PhotoDatabase: RoomDatabase() {

    abstract val photoDao: PhotoDao

}
