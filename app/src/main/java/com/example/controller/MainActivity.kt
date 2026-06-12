package com.example.controller

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.util.Log
import java.util.UUID
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.controller.ui.theme.ControllerTheme


var resistance by mutableIntStateOf(50)
var abs_resistance by mutableIntStateOf(50)
var hue by mutableFloatStateOf(60f)
var max_resistance by mutableIntStateOf(100)
var min_resistance by mutableIntStateOf(0)
var resistance_step by mutableIntStateOf(1)

var actualResistance by mutableIntStateOf(50)
var actualCadence by mutableIntStateOf(0)
var actualPower by mutableIntStateOf(0)

val FTMS_SERVICE_UUID: UUID = UUID.fromString("00001826-0000-1000-8000-00805f9b34fb")
val SUPPORTED_RESISTANCE_LEVEL_RANGE_UUID: UUID = UUID.fromString("00002ad6-0000-1000-8000-00805f9b34fb")
val FTMS_CONTROL_POINT_UUID: UUID = UUID.fromString("00002ad9-0000-1000-8000-00805f9b34fb")
val RESISTANCE_LEVEL_UUID: UUID = UUID.fromString("00002ad1-0000-1000-8000-00805f9b34fb")
val INDOOR_BIKE_DATA_UUID: UUID = UUID.fromString("00002ad2-0000-1000-8000-00805f9b34fb")
val CCC_DESCRIPTOR_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ControllerTheme {
                val context = LocalContext.current
                var showBleDialog by remember { mutableStateOf(false) }
                var showLocationRationale by remember { mutableStateOf(false) }
                var showBluetoothRequest by remember {mutableStateOf(false) }
                val discoveredDevices = remember { mutableStateListOf<BluetoothDevice>() }
                var isScanning by remember { mutableStateOf(false) }

                val bluetoothManager = remember { context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager }
                val bluetoothAdapter = remember { bluetoothManager.adapter }
                var bluetoothGatt by remember { mutableStateOf<BluetoothGatt?>(null) }
                var isConnected by remember { mutableStateOf(false) }

                val colourBackground = Color(0xfff5f9f8)
                val colourPlus1 = Color.hsl(0f,0.40f,0.75f)
                val colourPlus5 = Color.hsl(0f,0.40f,0.65f)
                val colourPlus10 = Color.hsl(0f,0.40f,0.55f)
                val colourMinus1 = Color.hsl(115f,0.40f,0.75f)
                val colourMinus5 = Color.hsl(115f,0.40f,0.65f)
                val colourMinus10 = Color.hsl(115f,0.40f,0.55f)

                val permissions = remember {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        arrayOf(
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                    } else {
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }

                val gattCallback = remember {
                    object : BluetoothGattCallback() {
                        @SuppressLint("MissingPermission")
                        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                Log.i("BLE", "Connected to GATT server.")
                                isConnected = true
                                gatt.discoverServices()
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                Log.i("BLE", "Disconnected from GATT server.")
                                isConnected = false
                                gatt.close()
                                if (bluetoothGatt == gatt) {
                                    bluetoothGatt = null
                                }
                            }
                        }

                        @SuppressLint("MissingPermission")
                        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                Log.i("BLE", "Services discovered")
                                val ftmsService = gatt.getService(FTMS_SERVICE_UUID)
                                
                                // 1. Read the resistance range
                                val rangeChar = ftmsService?.getCharacteristic(SUPPORTED_RESISTANCE_LEVEL_RANGE_UUID)
                                if (rangeChar != null) {
                                    gatt.readCharacteristic(rangeChar)
                                }
                            } else {
                                Log.w("BLE", "onServicesDiscovered received: $status")
                            }
                        }

                        @SuppressLint("MissingPermission")
                        override fun onDescriptorWrite(
                            gatt: BluetoothGatt,
                            descriptor: BluetoothGattDescriptor,
                            status: Int
                        ) {
                            Log.d("BLE", "onDescriptorWrite called for ${descriptor.characteristic.uuid} with status: $status")
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                if (descriptor.characteristic.uuid == FTMS_CONTROL_POINT_UUID) {
                                    Log.d("BLE", "Control Point indications enabled. Now enabling Bike Data notifications...")
                                    // 3. Enable Notifications for Indoor Bike Data (Telemetry)
                                    // We do this AFTER Control Point indications are enabled.
                                    val ftmsService = gatt.getService(FTMS_SERVICE_UUID)
                                    val bikeDataChar = ftmsService?.getCharacteristic(INDOOR_BIKE_DATA_UUID)
                                    if (bikeDataChar != null) {
                                        gatt.setCharacteristicNotification(bikeDataChar, true)
                                        val bikeDescriptor = bikeDataChar.getDescriptor(CCC_DESCRIPTOR_UUID)
                                        if (bikeDescriptor != null) {
                                            @Suppress("DEPRECATION")
                                            bikeDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                            gatt.writeDescriptor(bikeDescriptor)
                                        }
                                    }
                                } else if (descriptor.characteristic.uuid == INDOOR_BIKE_DATA_UUID) {
                                    Log.d("BLE", "Bike Data notifications enabled. Now requesting control...")
                                    // 4. Request Control (Opcode 0x00) only AFTER all notifications are enabled
                                    // This is the final step in the handshake.
                                    val ftmsService = gatt.getService(FTMS_SERVICE_UUID)
                                    val controlPoint = ftmsService?.getCharacteristic(FTMS_CONTROL_POINT_UUID)
                                    if (controlPoint != null) {
                                        @Suppress("DEPRECATION")
                                        controlPoint.value = byteArrayOf(0x00)
                                        @Suppress("DEPRECATION")
                                        gatt.writeCharacteristic(controlPoint)
                                    }
                                }
                            } else {
                                Log.e("BLE", "Descriptor write failed: $status")
                            }
                        }

                        @SuppressLint("MissingPermission")
                        override fun onCharacteristicWrite(
                            gatt: BluetoothGatt,
                            characteristic: BluetoothGattCharacteristic,
                            status: Int
                        ) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                if (characteristic.uuid == FTMS_CONTROL_POINT_UUID) {
                                    Log.d("BLE", "Successfully wrote to Control Point")
                                    // Request a read of the current resistance level for verification
                                    val ftmsService = gatt.getService(FTMS_SERVICE_UUID)
                                    val resistanceChar = ftmsService?.getCharacteristic(RESISTANCE_LEVEL_UUID)
                                    if (resistanceChar != null) {
                                        gatt.readCharacteristic(resistanceChar)
                                    }
                                }
                            } else {
                                Log.e("BLE", "Characteristic write failed with status: $status")
                            }
                        }

                        @Suppress("DEPRECATION")
                        @SuppressLint("MissingPermission")
                        override fun onCharacteristicRead(
                            gatt: BluetoothGatt,
                            characteristic: BluetoothGattCharacteristic,
                            status: Int
                        ) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                val data = characteristic.value
                                if (characteristic.uuid == SUPPORTED_RESISTANCE_LEVEL_RANGE_UUID) {
                                    if (data != null && data.size >= 6) {
                                        // Min Resistance (Sint16) - Little Endian
                                        val min = (data[1].toInt() shl 8) or (data[0].toInt() and 0xFF)
                                        // Max Resistance (Sint16) - Little Endian
                                        val max = (data[3].toInt() shl 8) or (data[2].toInt() and 0xFF)
                                        // Increment (Uint16) - Little Endian
                                        val step = ((data[5].toInt() and 0xFF) shl 8) or (data[4].toInt() and 0xFF)
                                        
                                        min_resistance = min
                                        max_resistance = max
                                        resistance_step = step
                                        Log.d("BLE", "Resistance Range: $min - $max, Step: $step")
                                    }

                                    // 2. NOW enable Indications for Control Point, after Range Read is finished.
                                    // We will enable Bike Data notifications after this finishes in onDescriptorWrite.
                                    val ftmsService = gatt.getService(FTMS_SERVICE_UUID)
                                    val controlPoint = ftmsService?.getCharacteristic(FTMS_CONTROL_POINT_UUID)
                                    if (controlPoint != null) {
                                        gatt.setCharacteristicNotification(controlPoint, true)
                                        val descriptor = controlPoint.getDescriptor(CCC_DESCRIPTOR_UUID)
                                        if (descriptor != null) {
                                            @Suppress("DEPRECATION")
                                            descriptor.value = BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                            @Suppress("DEPRECATION")
                                            gatt.writeDescriptor(descriptor)
                                            Log.d("BLE", "Range read finished. Now enabling indications for Control Point...")
                                        }
                                    }
                                } else if (characteristic.uuid == RESISTANCE_LEVEL_UUID) {
                                    if (data != null && data.size >= 1) {
                                        val currentRes = data[0].toInt() and 0xFF
                                        Log.d("BLE", "Current Resistance from Machine: $currentRes")
                                    }
                                }
                            } else {
                                Log.w("BLE", "onCharacteristicRead failed: $status for ${characteristic.uuid}")
                            }
                        }

                        @Suppress("DEPRECATION")
                        override fun onCharacteristicChanged(
                            gatt: BluetoothGatt,
                            characteristic: BluetoothGattCharacteristic
                        ) {
                            // 1. Handle Telemetry Data pushed from the bike
                            if (characteristic.uuid == INDOOR_BIKE_DATA_UUID) {
                                val data = characteristic.value
                                if (data != null && data.size >= 4) {
                                    Log.d("BLE", "Indoor Bike Data received: ${data.contentToString()}")
                                    val flags = ((data[1].toInt() and 0xFF) shl 8) or (data[0].toInt() and 0xFF)
                                    var offset = 2
                                    
                                    // Speed is mandatory in FTMS Indoor Bike Data (Unit 0.01km/h)
                                    offset += 2 

                                    // Bit 2: Instantaneous Cadence present
                                    if ((flags and 0x04) != 0 && data.size >= offset + 2) {
                                        val rawCadence = ((data[offset+1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
                                        actualCadence = rawCadence / 2 // Unit is 0.5
                                        offset += 2
                                    }

                                    // Skip Average Cadence (Bit 3)
                                    if ((flags and 0x08) != 0) offset += 2
                                    // Skip Total Distance (Bit 4)
                                    if ((flags and 0x10) != 0) offset += 3
                                    
                                    // Bit 5: Resistance Level present
                                    if ((flags and 0x20) != 0 && data.size >= offset + 2) {
                                        val rawRes = ((data[offset+1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
                                        actualResistance = rawRes
                                        offset += 2
                                    }

                                    // Bit 6: Instantaneous Power present
                                    if ((flags and 0x40) != 0 && data.size >= offset + 2) {
                                        val rawPower = ((data[offset+1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
                                        actualPower = rawPower
                                        offset += 2
                                    }
                                }
                            }
                            // 2. Handle Responses from the Control Point (e.g. handshake result, resistance update result)
                            else if (characteristic.uuid == FTMS_CONTROL_POINT_UUID) {
                                val data = characteristic.value
                                if (data != null && data.size >= 3) {
                                    val requestedOp = data[1].toInt() and 0xFF
                                    val result = data[2].toInt() and 0xFF
                                    Log.d("BLE", "Control Point Response: Opcode $requestedOp, Result $result (1=Success)")
                                }
                            }

                            if (abs_resistance!=actualResistance){updateResistance(0,bluetoothGatt)}
                        }
                    }
                }

                val scanCallback = remember {
                    object : ScanCallback() {
                        @SuppressLint("MissingPermission")
                        override fun onScanResult(callbackType: Int, result: ScanResult) {
                            val device = result.device
                            if (device !in discoveredDevices) {
                                discoveredDevices.add(device)
                                Log.d("BLE", "Found device: ${device.name ?: "Unknown"} - ${device.address}")
                            }
                        }

                        override fun onScanFailed(errorCode: Int) {
                            Log.e("BLE", "Scan failed with error code: $errorCode")
                        }
                    }
                }

                @SuppressLint("MissingPermission")
                fun disconnectDevice() {
                    bluetoothGatt?.disconnect()
                }

                @SuppressLint("MissingPermission")
                fun startScan() {
                    Log.d("BLE", "startScan() called")
                    if (bluetoothAdapter == null) {
                        Log.e("BLE", "BluetoothAdapter is null")
                        return
                    }
                    if (!bluetoothAdapter.isEnabled) {
                        showBluetoothRequest = true
                        Log.e("BLE", "Bluetooth is disabled")
                        return
                    }
                    else {
                        showBleDialog = true
                        val scanner = bluetoothAdapter.bluetoothLeScanner
                        if (scanner == null) {
                            Log.e("BLE", "BluetoothLeScanner is null")
                            return
                        }

                        // Filter for Fitness Machine Service (FTMS) UUID: 0x1826
                        val filters = listOf(
                            ScanFilter.Builder()
                                .setServiceUuid(ParcelUuid.fromString("00001826-0000-1000-8000-00805f9b34fb"))
                                .build()
                        )

                        val settings = ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .build()

                        discoveredDevices.clear()
                        isScanning = true
                        scanner.startScan(filters, settings, scanCallback)
                        Log.d("BLE", "Scanning started successfully for Fitness Machines")
                    }
                }

                @SuppressLint("MissingPermission")
                fun stopScan() {
                    isScanning = false
                    bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
                }

                //Permission checking logic - if required location permissions aren't granted
                //when connect button is clicked, showLocationRationale is set to true and dialogs
                //below are shown.

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissionsMap ->
                    val granted = permissionsMap.entries.all { it.value }
                    if (granted) {
                        showBleDialog = true
                    } else {
                        Log.e("BLE", "Permissions denied")
                    }
                }

                if (showLocationRationale) {
                    AlertDialog(
                        onDismissRequest = { showLocationRationale = false },
                        title = { Text("Location Permission Required") },
                        text = { Text("This app needs location permission to scan for nearby Bluetooth devices.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLocationRationale = false
                                permissionLauncher.launch(permissions)
                            }) {
                                Text("Grant")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLocationRationale = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showBluetoothRequest) {
                    AlertDialog(
                        onDismissRequest = { showBluetoothRequest = false },
                        title = { Text("Bluetooth Required") },
                        text = { Text("Bluetooth must be enabled to scan for devices.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showBluetoothRequest = false
                               }) {
                                Text("OK")
                            }
                        }

                    )
                }

                if (showBleDialog) {
                    BleDeviceDialog(
                        devices = discoveredDevices,
                        isScanning = isScanning,
                        onDismiss = {
                            stopScan()
                            showBleDialog = false
                        },
                        onScanToggle = {
                            if (isScanning) stopScan() else startScan()
                        },
                        onDeviceSelected = { device ->
                            @SuppressLint("MissingPermission")
                            fun connectToDevice(device: BluetoothDevice) {
                                bluetoothGatt?.disconnect()
                                bluetoothGatt?.close()
                                bluetoothGatt = device.connectGatt(context, false, gattCallback)
                            }
                            stopScan()
                            showBleDialog = false
                            Log.d("BLE", "Selected device: ${device.name ?: device.address}")
                            connectToDevice(device)
                        }
                    )
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = colourBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Label(value = actualResistance.toString().plus("%"), fontSize = 18.sp)
                            Label(value = actualCadence.toString().plus("rpm"), fontSize = 18.sp)
                            Label(value = actualPower.toString().plus("W"), fontSize = 18.sp)
                        }

                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            //TODO update colours
                            MyButton(
                                onClick = { updateResistance(10, bluetoothGatt)},
                                label = "+10",
                                backgroundColor = colourPlus10,
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(5, bluetoothGatt)},
                                label = "+5",
                                backgroundColor = colourPlus5,
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(1, bluetoothGatt)},
                                label = "+1",
                                backgroundColor = colourPlus1,
                                width = 150.dp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 5.dp)
                                    .size(250.dp)
                                    .drawBehind {
                                        //TODO adjust this to go from 3f to 2f as percentage changes
                                        val radius = size.minDimension / 3f
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                0.0f to colourBackground,
                                                0.75f to colourBackground,
                                                0.80f to Color(0xff5f5fff),
                                                1.0f to Color.Transparent,
                                                radius = radius
                                            ),
                                                /*brush = Brush.radialGradient(
                                                0.0f to Color.hsl(hue, 0.85f, 0.90f),
                                                0.65f to Color.hsl(hue, 0.85f, 0.90f),
                                                0.70f to Color.hsl(hue, 0.85f, 0.50f),
                                                1.0f to Color.Transparent,
                                                radius = radius
                                            ),*/
                                            radius = radius
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Label(
                                    value = resistance.toString().plus("%"),
                                    fontSize = 48.sp
                                )
                            }

                            MyButton(
                                onClick = { updateResistance(-1, bluetoothGatt)},
                                label = "-1",
                                backgroundColor = colourMinus1,
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(-5, bluetoothGatt)},
                                label = "-5",
                                backgroundColor = colourMinus5,
                                width = 150.dp
                            )
                            MyButton(
                                onClick = { updateResistance(-10, bluetoothGatt)},
                                label = "-10",
                                backgroundColor = colourMinus10,
                                width = 150.dp
                            )
                        }

                        MyButton(
                            onClick = {
                                if (isConnected) {
                                    disconnectDevice()
                                } else {
                                    // 1: Check permissions for location services
                                    val allGranted = permissions.all {
                                        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                                    }
                                    if (allGranted) {

                                        startScan()
                                    } else {
                                        showLocationRationale = true
                                    }
                                }
                            },
                            label = if (isConnected) "Disconnect" else "Connect",
                            backgroundColor = Color(red = 200, green = 200, blue = 200),
                            textColor = Color.Black,
                            width = 150.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                        )
                    }
                }
            }
        }
    }
}

fun updateResistance(value: Int, gatt: BluetoothGatt? = null) {
    resistance += value
    if (resistance>100) { resistance = 100 }
    if (resistance < 0) { resistance = 0 }
    hue = (120 - resistance * 1.2).toFloat()
    abs_resistance = ((max_resistance - min_resistance) * (resistance / 100.0) + min_resistance).toInt()
    
    if (gatt != null) {
        sendResistanceToMachine(gatt)
    }
}

@SuppressLint("MissingPermission")
fun sendResistanceToMachine(gatt: BluetoothGatt) {
    val service = gatt.getService(FTMS_SERVICE_UUID)
    val controlPoint = service?.getCharacteristic(FTMS_CONTROL_POINT_UUID)
    if (controlPoint != null) {
        // FTMS Set Target Resistance Level: Opcode 0x04, then SInt16 value (Little Endian)
        val data = ByteArray(3)
        data[0] = 0x04
        data[1] = (abs_resistance and 0xFF).toByte()
        data[2] = (abs_resistance shr 8 and 0xFF).toByte()
        
        @Suppress("DEPRECATION")
        controlPoint.value = data
        @Suppress("DEPRECATION")
        gatt.writeCharacteristic(controlPoint)
        Log.d("BLE", "Sent resistance: $abs_resistance")
    }
}

@Composable
fun BleDeviceDialog(
    devices: List<BluetoothDevice>,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onScanToggle: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Select Device")
                if (isScanning) {
                    Spacer(Modifier.width(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                }
            }
        },
        text = {
            Column {
                Button(
                    onClick = onScanToggle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isScanning) "Stop Scan" else "Start Scan")
                }
                Spacer(Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(devices) { device ->
                        @SuppressLint("MissingPermission")
                        val deviceName = device.name ?: "Unknown Device"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onDeviceSelected(device) }
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(deviceName, style = MaterialTheme.typography.bodyLarge)
                                Text(device.address, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun Label(
    value: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified
) {
    Text(
        text = value,
        modifier = modifier,
        fontSize = fontSize
    )
}

@Composable
fun MyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Click Me",
    width: Dp? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    Button(
        onClick = onClick,
        modifier = if (width != null) modifier.width(width) else modifier,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: ButtonDefaults.buttonColors().containerColor,
            contentColor = textColor ?: ButtonDefaults.buttonColors().contentColor
        )
    ) {
        Text(text = label)
    }
}
