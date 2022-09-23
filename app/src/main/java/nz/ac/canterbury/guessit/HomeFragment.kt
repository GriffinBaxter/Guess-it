package nz.ac.canterbury.guessit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import nz.ac.canterbury.guessit.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val showPhotoButton: Button = view.findViewById(R.id.showPhotoButton)
        showPhotoButton.setOnClickListener {
            // TODO
        }

        val guessPhotoButton: Button = view.findViewById(R.id.guessPhotoButton)
        guessPhotoButton.setOnClickListener {
            // TODO
        }

        return view
    }
}