package nz.ac.canterbury.guessit.ui.showPhoto

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import nz.ac.canterbury.guessit.database.Photo
import nz.ac.canterbury.guessit.database.PhotoRepository
import nz.ac.canterbury.guessit.database.PhotoRepositoryImpl
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
): ViewModel() {

    val photos: LiveData<List<Photo>> = photoRepository.photos.asLiveData()
    val numPhotos: LiveData<Int> = photoRepository.numPhotos.asLiveData()

    fun addPhoto(photo: Photo) = viewModelScope.launch {
        photoRepository.insert(photo)
    }

}
