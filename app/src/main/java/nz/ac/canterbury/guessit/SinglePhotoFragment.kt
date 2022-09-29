package nz.ac.canterbury.guessit

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

class SinglePhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_photo, container, false)

        val photoPath = arguments?.getString("photoPath")!!
        val bitmap = BitmapFactory.decodeFile(photoPath)
        val singlePhotoImageView: ImageView = view.findViewById(R.id.singlePhotoView)
        singlePhotoImageView.setImageBitmap(bitmap)

        return view
    }
}
