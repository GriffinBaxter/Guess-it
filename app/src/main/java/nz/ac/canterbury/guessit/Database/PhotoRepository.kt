package nz.ac.canterbury.guessit.Database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class PhotoRepository(private val photoDao: PhotoDao) {
    val photos: Flow<List<Photo>> = photoDao.getAll()
    val numPhotos: Flow<Int> = photoDao.getCount()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(photo: Photo) {
        photoDao.insert(photo)
    }
}
