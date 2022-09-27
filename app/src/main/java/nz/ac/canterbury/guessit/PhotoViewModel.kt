package nz.ac.canterbury.guessit

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class PhotoViewModel(private val photoRepository: PhotoRepository): ViewModel() {

    val photos: LiveData<List<Photo>> = photoRepository.photos.asLiveData()
    val numPhotos: LiveData<Int> = photoRepository.numPhotos.asLiveData()

    fun addPhoto(photo: Photo) = viewModelScope.launch {
        photoRepository.insert(photo)
    }

}

class PhotoViewModelFactory(private val repository: PhotoRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PhotoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PhotoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
