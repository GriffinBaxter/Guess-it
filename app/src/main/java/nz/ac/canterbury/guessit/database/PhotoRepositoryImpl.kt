package nz.ac.canterbury.guessit.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepositoryImpl @Inject constructor(
    private val db: PhotoDatabase
    ): PhotoRepository {
    private val dao = db.photoDao

    override val photos: Flow<List<Photo>> = dao.getAll()
    override val numPhotos: Flow<Int> = dao.getCount()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    override suspend fun insert(photo: Photo) {
        dao.insert(photo)
    }
}
