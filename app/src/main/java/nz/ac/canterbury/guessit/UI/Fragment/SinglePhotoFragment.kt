package nz.ac.canterbury.guessit.UI.Fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import nz.ac.canterbury.guessit.Controller.ImageLabeler
import nz.ac.canterbury.guessit.MainActivity
import nz.ac.canterbury.guessit.R

class SinglePhotoFragment : Fragment() {

    lateinit var imageLabeler: ImageLabeler

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_photo, container, false)

        imageLabeler = ImageLabeler(activity as MainActivity)

        val photoPath = arguments?.getString("photoPath")!!
        val singlePhotoImageView: ImageView = view.findViewById(R.id.singlePhotoView)
        Picasso.get().load("file:$photoPath").into(singlePhotoImageView)

        val latitude = arguments?.getString("latitude")!!.toDouble()
        val longitude = arguments?.getString("longitude")!!.toDouble()

        val photoDescription = imageLabeler.setPhotoDescription(BitmapFactory.decodeFile(photoPath))

        return view
    }
}
