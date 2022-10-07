package nz.ac.canterbury.guessit.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
data class Photo(
    @PrimaryKey val file: String,
    val latitude: Double,
    val longitude: Double
    )
