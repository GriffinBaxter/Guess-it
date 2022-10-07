package nz.ac.canterbury.guessit.database

import kotlinx.coroutines.flow.Flow

interface PhotoRepository {
    val photos: Flow<List<Photo>>
    val numPhotos: Flow<Int>
    suspend fun insert(photo: Photo)
}