package nz.ac.canterbury.guessit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import nz.ac.canterbury.guessit.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_home)
    }
}
