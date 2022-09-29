package nz.ac.canterbury.guessit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso

class SinglePhotoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_photo, container, false)

        val photoPath = arguments?.getString("photoPath")!!
        val singlePhotoImageView: ImageView = view.findViewById(R.id.singlePhotoView)
        Picasso.get().load("file:$photoPath").into(singlePhotoImageView)

        return view
    }
}
