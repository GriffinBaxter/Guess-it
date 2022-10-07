package nz.ac.canterbury.guessit.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import nz.ac.canterbury.guessit.R

class SearchFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val searchText: TextView = view.findViewById(R.id.searchText)
        val searchType = arguments?.getString("searchType")!!
        if (searchType == "host") {
            searchText.text = resources.getString(R.string.searching_for_player)
        } else if (searchType == "player") {
            searchText.text = resources.getString(R.string.searching_for_host)
        }

        // TODO: navigate when connection established (dependent on searchType)

        return view
    }

}