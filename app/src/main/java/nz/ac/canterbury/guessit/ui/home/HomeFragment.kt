package nz.ac.canterbury.guessit.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import nz.ac.canterbury.guessit.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val showPhotoButton: Button = view.findViewById(R.id.showPhotoButton)
        showPhotoButton.setOnClickListener {
            val args = bundleOf(
                "searchType" to "host"
            )
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_searchFragment, args)
        }

        val guessPhotoButton: Button = view.findViewById(R.id.guessPhotoButton)
        guessPhotoButton.setOnClickListener {
            val args = bundleOf(
                "searchType" to "player"
            )
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_searchFragment, args)
        }

//        val showBluetoothButton: Button = view.findViewById(R.id.showBluetoothButton)
//        showBluetoothButton.setOnClickListener {
//            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_BluetoothFragment2)
//        }

        val showNearbyButton: Button = view.findViewById(R.id.showNearbyButton)
        showNearbyButton.setOnClickListener {
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_nearbyFragment)
        }

        return view
    }
}
