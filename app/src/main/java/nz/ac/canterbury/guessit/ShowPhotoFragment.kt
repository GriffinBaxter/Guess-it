package nz.ac.canterbury.guessit

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

private const val REQUEST_CAMERA = 110
private const val REQUEST_GALLERY = 111

class ShowPhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_photo, container, false)

        val takePhotoButton: Button = view.findViewById(R.id.takePhotoButton)
        takePhotoButton.setOnClickListener {
            takePhoto()
        }

        val choosePhotoButton: Button = view.findViewById(R.id.choosePhotoButton)
        choosePhotoButton.setOnClickListener {
            choosePhotoFromPhone()
        }

        val galleryButton: Button = view.findViewById(R.id.galleryButton)
        galleryButton.setOnClickListener {
            gallery()
        }

        return view
    }

    private fun takePhoto() {  // TODO: add photo to app gallery
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_CAMERA)
    }

    private fun choosePhotoFromPhone() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_GALLERY)
    }

    private fun gallery() {
        // TODO
    }
}
