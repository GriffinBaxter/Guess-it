package nz.ac.canterbury.guessit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.*

private const val REQUEST_CAMERA = 110
private const val REQUEST_GALLERY = 111

class ShowPhotoFragment : Fragment() {

    private lateinit var photosList: RecyclerView

    private val photoDirectory
        get() = File(requireContext().getExternalFilesDir(null), "guessit")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_photo, container, false)

        photosList = view.findViewById(R.id.photosList)
        photosList.layoutManager = GridLayoutManager(requireContext(), 5)

        loadPhotos()

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

    private fun loadPhotos() {
        if (photoDirectory.exists()) {
            val photos = photoDirectory
                .listFiles { file, _ -> file.isDirectory }
                ?.map { Photo(it) }
                ?.filter { it.file.exists() }

            photosList.adapter = photos?.let { PhotoAdapter(it) }
        }
    }

    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val uri = photoUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
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

    private fun photoUri(): Uri {
        val file = File(photoDirectory, "${UUID.randomUUID()}.jpg")  // TODO: change to an ordered ID scheme from database?
        return FileProvider.getUriForFile(
            requireContext(),
            "nz.ac.canterbury.guessit.fileprovider",
            file
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    loadPhotos()
                }
            }
            REQUEST_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        copyUriToUri(uri, photoUri())
                        loadPhotos()
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }
}
