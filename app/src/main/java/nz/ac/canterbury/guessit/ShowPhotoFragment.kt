package nz.ac.canterbury.guessit

import android.app.Activity
import android.content.Intent
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.IOException
import java.util.*

private const val REQUEST_CAMERA = 110
private const val REQUEST_GALLERY = 111

class ShowPhotoFragment : Fragment(), PhotoAdapter.OnPhotoListener {

    private lateinit var photosList: RecyclerView

    private val viewModel: PhotoViewModel by activityViewModels() {
        PhotoViewModelFactory((requireActivity().application as SENGApplication).repository)
    }

    private val photoDirectory
        get() = File(requireContext().getExternalFilesDir(null), "guessit")

    private var currentPhotoPath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_photo, container, false)

        if (!photoDirectory.exists()) {
            photoDirectory.mkdirs()
        }

        val photoAdapter = PhotoAdapter(listOf(), this)
        viewModel.photos.observe(viewLifecycleOwner) { newPhotos ->
            photoAdapter.setData(newPhotos)
        }

        photosList = view.findViewById(R.id.photosList)
        photosList.layoutManager = GridLayoutManager(requireContext(), 5)
        photosList.adapter = photoAdapter

        val takePhotoButton: Button = view.findViewById(R.id.takePhotoButton)
        takePhotoButton.setOnClickListener {
            takePhoto()
        }

        val choosePhotoButton: Button = view.findViewById(R.id.choosePhotoButton)
        choosePhotoButton.setOnClickListener {
            choosePhotoFromPhone()
        }

        return view
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val file = File(photoDirectory, "${UUID.randomUUID()}.jpg")
        val uri = photoUri(file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        currentPhotoPath = file.absolutePath
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun choosePhotoFromPhone() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun copyUriToUri(from: Uri, to: Uri) {
        requireContext().contentResolver.openInputStream(from).use { input ->
            requireContext().contentResolver.openOutputStream(to).use { output ->
                input!!.copyTo(output!!)
            }
        }
    }

    private fun photoUri(file: File): Uri {
        return FileProvider.getUriForFile(
            requireContext(),
            "nz.ac.canterbury.guessit.fileprovider",
            file
        )
    }

    private fun getPhotoLocation(file: File): FloatArray? {
        try {
            val exifInterface = ExifInterface(file)
            val latitudeLongitude = FloatArray(2)
            if (exifInterface.getLatLong(latitudeLongitude)) {
                return latitudeLongitude
            }
        } catch (e: IOException) {
            e.message?.let { Log.e("Couldn't read exif info: ", it) }
        }
        Toast.makeText(context, "Selected photo has no location data available.", Toast.LENGTH_LONG).show()
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel.addPhoto(Photo(currentPhotoPath, 0.toFloat(), 0.toFloat()))  // TODO: get location when taking photo
                }
            }
            REQUEST_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        val file = File(photoDirectory, "${UUID.randomUUID()}.jpg")
                        val photoUri = photoUri(file)
                        copyUriToUri(uri, photoUri)
                        val photoLocation = getPhotoLocation(file)
                        if (photoLocation != null) {
                            viewModel.addPhoto(Photo(file.absolutePath, photoLocation[0], photoLocation[1]))
                        }
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onPhotoClick(position: Int) {
        // TODO
    }
}
