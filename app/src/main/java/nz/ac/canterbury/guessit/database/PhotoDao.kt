package nz.ac.canterbury.guessit.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {

    @Insert
    suspend fun insert(photo: Photo): Long

    @Query("SELECT * FROM photo")
    fun getAll(): Flow<List<Photo>>

    @Query("SELECT COUNT(*) FROM photo")
    fun getCount(): Flow<Int>

}
