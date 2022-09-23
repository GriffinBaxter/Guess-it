package nz.ac.canterbury.guessit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val showPhotoButton: Button = view.findViewById(R.id.showPhotoButton)
        showPhotoButton.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_showPhoto)
        }

        return view
    }
}
