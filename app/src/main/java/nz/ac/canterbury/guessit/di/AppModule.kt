package nz.ac.canterbury.guessit.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import nz.ac.canterbury.guessit.database.PhotoDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providePhotoDatabase(app: Application): PhotoDatabase {
        return Room.databaseBuilder(
            app,
            PhotoDatabase::class.java,
            "photo_database"
        ).build()
    }
}