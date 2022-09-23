package nz.ac.canterbury.guessit

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.w3c.dom.Text

class BluetoothFragment : Fragment() {

    private val REQUEST_CODE_ENABLE_BT = 1
    private val REQUEST_CODE_DISCOVERABLE_BT = 2
    lateinit var bluetoothAdapter: BluetoothAdapter

    companion object {
        fun newInstance() = BluetoothFragment()
    }

    private lateinit var viewModel: BluetoothViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bluetooth, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(BluetoothViewModel::class.java)
        // TODO: Use the ViewModel

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            ))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBtIntent)
        }

        val bluetoothManager = context?.getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        val statusTextView = view.findViewById(R.id.bluetoothStatusText) as TextView
        statusTextView.text = "test"

        if (bluetoothAdapter.isEnabled) {
            statusTextView.text = "Enabled"
        } else {
            statusTextView.text = "Disabled"
        }

        val turnOnButton = view.findViewById(R.id.turnOnButton) as Button

        turnOnButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                // Already enabled
                Toast.makeText(context, "Already enabled!", Toast.LENGTH_LONG).show()
            } else {
                // Turn on bluetooth
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startForResult.launch(intent)
            }
        }

        val turnOffButton = view.findViewById(R.id.turnOffButton) as Button

        turnOffButton.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                // Already enabled
                Toast.makeText(context, "Already off!", Toast.LENGTH_LONG).show()
            } else {
                // Turn on bluetooth
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@setOnClickListener
                }
                bluetoothAdapter.disable()
                Toast.makeText(context, "Bluetooth now turned off", Toast.LENGTH_LONG).show()
            }
        }

        val discoverableButton = view.findViewById(R.id.discoverableButton) as Button

        discoverableButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return@setOnClickListener
            }
            if (!bluetoothAdapter.isDiscovering) {
                Toast.makeText(context, "Making your device discoverable", Toast.LENGTH_LONG).show()
                val intent = Intent(Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE))
                startForResult2.launch(intent)
            }
        }

        val pairedButton = view.findViewById(R.id.pairedButton) as Button
        val pairedTextView = view.findViewById(R.id.pairedDevs) as TextView

        pairedButton.setOnClickListener {
            if (bluetoothAdapter.isEnabled) {
                pairedTextView.text = "Paired Devices"
                val devices = bluetoothAdapter.bondedDevices
                for (device in devices) {
                    val deviceName = device.name
                    val deviceAddress = device
                    pairedTextView.append("\nDevice: $deviceName, $deviceAddress")
                }
            }
        }


    }

    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //granted
            Log.e("PERMISSIONS", "BT granted")
        }else{
            //deny
            Log.e("PERMISSIONS", "BT denied")
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    private val startForResult2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            // Handle the Intent

        }
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            // Handle the Intent
            Toast.makeText(context, "Bluetooth is now on", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Could not turn on bluetooth", Toast.LENGTH_LONG).show()
        }
    }


}