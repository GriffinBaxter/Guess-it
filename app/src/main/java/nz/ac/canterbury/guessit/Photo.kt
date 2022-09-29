package nz.ac.canterbury.guessit

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo")
class Photo(
    @PrimaryKey @ColumnInfo val file: String,
    @ColumnInfo val latitude: Double,
    @ColumnInfo val longitude: Double)
