package nz.ac.canterbury.guessit.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import nz.ac.canterbury.guessit.controller.NearbyConnectionManager
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

    @Provides
    @Singleton
    fun provideNearbyConnectionManager(@ApplicationContext appContext: Context): NearbyConnectionManager {
        return NearbyConnectionManager(appContext)
    }
}