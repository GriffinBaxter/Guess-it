package nz.ac.canterbury.guessit.ui.showPhoto

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import nz.ac.canterbury.guessit.*
import nz.ac.canterbury.guessit.database.Photo
import java.io.*
import java.util.*


private const val REQUEST_CAMERA = 110
private const val REQUEST_GALLERY = 111

@AndroidEntryPoint
class ShowPhotoFragment : Fragment(), PhotoAdapter.OnPhotoListener {

    private lateinit var photosList: RecyclerView

    private val photoViewModel: PhotoViewModel by viewModels()

    private val photoDirectory
        get() = File(requireContext().getExternalFilesDir(null), "guessit")

    private val hasLocationPermissions
        get() = requireActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private var currentPhotoPath = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_photo, container, false)

        if (!photoDirectory.exists()) {
            photoDirectory.mkdirs()
        }

        val photoAdapter = PhotoAdapter(listOf(), this)
        photoViewModel.photos.observe(viewLifecycleOwner) { newPhotos ->
            photoAdapter.setData(newPhotos)
        }

        photosList = view.findViewById(R.id.photosList)

        val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val galleryWidth = preferences.getString("galleryWidth", "3")?.toInt() ?: 3
        photosList.layoutManager = GridLayoutManager(requireContext(), galleryWidth)

        photosList.adapter = photoAdapter

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

    private fun takePhoto() {
        if (!hasLocationPermissions) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val file = File(photoDirectory, "${UUID.randomUUID()}.jpg")
            val uri = photoUri(file)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            currentPhotoPath = file.absolutePath
            startActivityForResult(intent, REQUEST_CAMERA)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (
            permissions.contains(Manifest.permission.ACCESS_FINE_LOCATION) &&
            grantResults[permissions.indexOf(Manifest.permission.ACCESS_FINE_LOCATION)] == 0
        ) {
            takePhoto()
        } else {
            Toast.makeText(context, getString(R.string.locationRequired), Toast.LENGTH_LONG).show()
        }
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

    private fun photoUri(file: File): Uri {
        return FileProvider.getUriForFile(
            requireContext(),
            "nz.ac.canterbury.guessit.fileprovider",
            file
        )
    }

    private fun getPhotoLocation(file: File): FloatArray? {
        try {
            val exifInterface = ExifInterface(file)
            val latitudeLongitude = FloatArray(2)
            if (exifInterface.getLatLong(latitudeLongitude)) {
                return latitudeLongitude
            }
        } catch (e: IOException) {
            e.message?.let { Log.e("Couldn't read exif info: ", it) }
        }
        Toast.makeText(context, getString(R.string.noLocationAvailable), Toast.LENGTH_LONG).show()
        return null
    }

    @SuppressLint("MissingPermission")
    private fun addPhotoWithCurrentLocation() {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                photoViewModel.addPhoto(
                    Photo(
                    currentPhotoPath,
                    location.latitude,
                    location.longitude,
                )
                )
                locationManager.removeUpdates(this)
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val provider = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            LocationManager.FUSED_PROVIDER
        } else {
            LocationManager.GPS_PROVIDER
        }
        if (hasLocationPermissions) {
            locationManager.requestLocationUpdates(provider, 0, 0f, listener)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    addPhotoWithCurrentLocation()
                }
            }
            REQUEST_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        val file = File(photoDirectory, "${UUID.randomUUID()}.jpg")
                        val photoUri = photoUri(file)
                        copyUriToUri(uri, photoUri)
                        val photoLocation = getPhotoLocation(file)
                        if (photoLocation != null) {
                            photoViewModel.addPhoto(
                                Photo(
                                file.absolutePath,
                                photoLocation[0].toDouble(),
                                photoLocation[1].toDouble(),
                            )
                            )
                        }
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun onPhotoClick(position: Int) {
        val photo = photoViewModel.photos.value!![position]
        val args = bundleOf(
            "photoPath" to photo.file, "latitude" to photo.latitude.toString(), "longitude" to photo.longitude.toString()
        )
        Navigation.findNavController(requireView()).navigate(R.id.action_showPhoto_to_singlePhotoFragment, args)
    }
}
