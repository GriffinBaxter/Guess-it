package nz.ac.canterbury.guessit

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.*
import java.util.*


private const val REQUEST_CAMERA = 110
private const val REQUEST_GALLERY = 111

class ShowPhotoFragment : Fragment(), PhotoAdapter.OnPhotoListener {

    private lateinit var photosList: RecyclerView

    private val viewModel: PhotoViewModel by activityViewModels() {
        PhotoViewModelFactory((requireActivity().application as SENGApplication).repository)
    }

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
        viewModel.photos.observe(viewLifecycleOwner) { newPhotos ->
            photoAdapter.setData(newPhotos)
        }

        photosList = view.findViewById(R.id.photosList)
        photosList.layoutManager = GridLayoutManager(requireContext(), 3)
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
            Toast.makeText(context, "The fine/precise location permission is required to take a photo.", Toast.LENGTH_LONG).show()
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
        Toast.makeText(context, "Selected photo has no location data available.", Toast.LENGTH_LONG).show()
        return null
    }

    @SuppressLint("MissingPermission")
    private fun addPhotoWithCurrentLocation() {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val thumbnailFile = getThumbnail(currentPhotoPath)
                viewModel.addPhoto(Photo(
                    currentPhotoPath,
                    thumbnailFile.absolutePath,
                    location.latitude,
                    location.longitude,
                ))
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
                        val thumbnailFile = getThumbnail(file.absolutePath)
                        val photoLocation = getPhotoLocation(file)
                        if (photoLocation != null) {
                            viewModel.addPhoto(Photo(
                                file.absolutePath,
                                thumbnailFile.absolutePath,
                                photoLocation[0].toDouble(),
                                photoLocation[1].toDouble(),
                            ))
                        }
                    }
                }
            }
            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun getThumbnail(filePath: String): File {
        val thumbnailFile = File(photoDirectory, "${UUID.randomUUID()}.jpg")
        val fis = FileInputStream(filePath)
        var thumbnailBitmap = BitmapFactory.decodeStream(fis)
        thumbnailBitmap = Bitmap.createScaledBitmap(
            thumbnailBitmap, 256, 256, false
        )
        val fileOutputStream = FileOutputStream(thumbnailFile)
        thumbnailBitmap.compress(
            Bitmap.CompressFormat.JPEG, 85, fileOutputStream
        )
        fileOutputStream.close()
        return thumbnailFile
    }

    override fun onPhotoClick(position: Int) {
        val photo = viewModel.photos.value!![position]
        val args = bundleOf("photoPath" to photo.file)
        Navigation.findNavController(requireView()).navigate(R.id.action_showPhoto_to_singlePhotoFragment, args)
    }
}
