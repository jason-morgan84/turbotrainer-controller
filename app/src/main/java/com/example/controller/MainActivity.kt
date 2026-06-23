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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.controller.ui.theme.ControllerTheme
import kotlin.math.round


var resistance by mutableIntStateOf(50)
var abs_resistance by mutableIntStateOf(50)
var max_resistance by mutableIntStateOf(100)
var min_resistance by mutableIntStateOf(0)
var resistance_step by mutableIntStateOf(1)

var actualResistance by mutableIntStateOf(50)
var actualCadence by mutableIntStateOf(0)
var actualPower by mutableIntStateOf(0)

var averagePower by mutableIntStateOf(0)

var count by mutableIntStateOf(1)

var actualEnergy by mutableIntStateOf(0)

var actualDistance by mutableDoubleStateOf(0.0)
var averageSpeed by mutableDoubleStateOf(0.0)

var totalPower by mutableIntStateOf(0)

var totalTime by mutableLongStateOf(0)
var currentTime by mutableLongStateOf(0)
var lastTime by mutableLongStateOf(0)


val maxAlpha = 1.0f
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
                val colourPlus1 = Color(0xff715fff)
                val colourPlus5 = Color(0xff835fff)
                val colourPlus10 = Color(0xff965fff)
                val colourMiddle = Color(0xff5f5fff)
                val colourMinus1 = Color(0xff7777e7)
                val colourMinus5 = Color(0xff8787d7)
                val colourMinus10 = Color(0xff9797c7)

                val gradientSteps = arrayOf(0,50,100)
                val gradientColours = arrayOf(colourMinus10, colourMiddle, colourPlus10)

                val gradient = remember(gradientColours) {
                    createGradient(gradientSteps, gradientColours)

                }
                val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.7f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = if (actualCadence > 0) 30000 / actualCadence else 500,
                            easing = LinearEasing
                        ),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "Alpha"
                )

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
                                    val flags = ((data[1].toInt() and 0xFF) shl 8) or (data[0].toInt() and 0xFF)
                                    
                                    // Log flags and raw data to identify active fields
                                    Log.d("BLE", "Flags (binary): ${flags.toString(2).padStart(16, '0')}")
                                    Log.d("BLE", "Indoor Bike Data received: ${data.contentToString()}")

                                    var offset = 2

                                    // Speed is mandatory in FTMS Indoor Bike Data (Unit 0.01km/h)
                                    offset += 2 

                                    // Bit 1: Average Speed present
                                    if ((flags and 0x02) != 0) offset += 2

                                    // Bit 2: Instantaneous Cadence present
                                    if ((flags and 0x04) != 0 && data.size >= offset + 2) {
                                        val rawCadence = ((data[offset+1].toInt() and 0xFF) shl 8) or (data[offset].toInt() and 0xFF)
                                        actualCadence = rawCadence / 2 // Unit is 0.5
                                        offset += 2
                                    }

                                    // Bit 3: Average Cadence present
                                    if ((flags and 0x08) != 0) offset += 2

                                    // Bit 4: Total Distance present
                                    if ((flags and 0x10) != 0 && data.size >= offset + 3) offset += 3

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
                            actualPower = resistance * 2
                            if (actualPower > 0)
                            {
                                totalPower += actualPower
                                if (currentTime == 0L)
                                {
                                    currentTime = System.currentTimeMillis()
                                    lastTime = currentTime
                                }
                                lastTime = currentTime
                                currentTime = System.currentTimeMillis()
                                totalTime += (currentTime - lastTime)
                                averagePower = totalPower / count
                                actualEnergy = ((totalPower*(totalTime/1000))/4184).toInt()
                                count += 1
                                //TODO: adjust average power/speed to take into account time between data

                                //0.5 * p * Cd * A * v^3 + m * G * Crr * V^2 = power
                                //p is roughly 1.2 at 18 degrees at sea level
                                //Typical CdA for average sized cyclist on hoods is 0.32 - 0.36
                                //G is roughly 10
                                //Take mass as 60kg
                                //Crr on smooth tarmac for reasonable road bike = 0.003-0.005
                                //ignoring v term as it relates to headwind (0 indoors)

                                // power = 0.2*v^3 + 2.4 * v^2
                                // power = v2(0.2 v + 2.4)
                                // may need to reduce power to ~5% to take into account mechanical loss

                                //newtons method:
                                //x1 = x0 - f(x0)/f'(x0)
                                //f(x) = 0.2*v^3 + 2.4 * v^2 - averagePower
                                //f'(x) = 0.6v^2 + 4.8v
                                //x0 = previous average speed
                                //x1 =


                                var x0 = 5.0
                                var dfdv = 0.2 * x0 * x0 * x0 + 2.4 *x0 *x0 - averagePower
                                var d2fdv2 = 0.6 * x0 * x0 + 4.8 * x0
                                var x1 = x0 - (dfdv/d2fdv2)

                                while (round(x0*10)/10!= round(x1*10)/10) {
                                    x0 = x1
                                    dfdv = 0.2 * x0 * x0 * x0 + 2.4 *x0 *x0 - averagePower
                                    d2fdv2 = 0.6 * x0 * x0 + 4.8 * x0
                                    x1 = x0 - (dfdv/d2fdv2)
                                }
                                averageSpeed = x1
                                Log.d("Stats","Average Speed: $x1")
                                actualDistance = averageSpeed*(totalTime/1000)

                            }


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
                //when connect button is clicked, showLocationRationale is set to true and dialogues
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

                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                                .border(
                                    width = 8.dp,
                                    color = colourPlus10,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .background(
                                    color = colourBackground,
                                    shape = RoundedCornerShape(24.dp)
                                ),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        )
                        {
                            Row(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth(0.6f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Label(value = actualPower.toString().plus(" W"), fontSize = 32.sp)
                                Label(value = actualCadence.toString().plus(" rpm"),fontSize = 32.sp)

                            }
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 12.dp,top = 4.dp)
                                    .fillMaxWidth(0.6f),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Label(value = averagePower.toString().plus(" W"), fontSize = 18.sp)
                                Label(value = (round(actualDistance/10)/100).toString().plus(" km"), fontSize = 18.sp)
                                Label(value = actualEnergy.toString().plus(" kcal"),fontSize = 18.sp)

                            }
                        }

                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            MyButton(
                                onClick = { updateResistance(10, bluetoothGatt)},
                                label = "+10",
                                backgroundColor = colourPlus10,
                                width = 150.dp,
                                roundCorners = 24.dp
                            )
                            MyButton(
                                onClick = { updateResistance(5, bluetoothGatt)},
                                label = "+5",
                                backgroundColor = colourPlus5,
                                width = 125.dp,
                                roundCorners = 24.dp
                            )
                            MyButton(
                                onClick = { updateResistance(1, bluetoothGatt)},
                                label = "+1",
                                backgroundColor = colourPlus1,
                                width = 100.dp,
                                roundCorners = 24.dp
                            )
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 5.dp)
                                    .size(200.dp)
                                    .drawBehind {

                                        val minRadius = 175.toFloat()
                                        val maxRadius = 225.toFloat()
                                        val radius = minRadius + (maxRadius - minRadius) * (resistance / 100.0).toFloat()
                                        val alpha = (if (actualCadence > 0) pulseAlpha else 1f) * maxAlpha
                                        val colour = gradient[resistance].copy(alpha = alpha)

                                        drawCircle(

                                            radius = radius,
                                            color = colour,
                                            style = Stroke(width = 8.dp.toPx())
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
                                width = 100.dp,
                                roundCorners = 24.dp
                            )
                            MyButton(
                                onClick = { updateResistance(-5, bluetoothGatt)},
                                label = "-5",
                                backgroundColor = colourMinus5,
                                width = 125.dp,
                                roundCorners = 24.dp
                            )
                            MyButton(
                                onClick = { updateResistance(-10, bluetoothGatt)},
                                label = "-10",
                                backgroundColor = colourMinus10,
                                width = 150.dp,
                                roundCorners = 24.dp
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
                            roundCorners = 12.dp,
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
    abs_resistance = ((max_resistance - min_resistance) * (resistance / 100.0) + min_resistance).toInt()
    
    if (gatt != null) {
        sendResistanceToMachine(gatt)
    }
}

fun createGradient(stepPercentage: Array<Int>, stepColor: Array<Color>): Array<Color> {
    val colourArray = Array(101) { Color.Transparent }

    for (currentStep in 0 until stepPercentage.size - 1) {
        val stepLength = stepPercentage[currentStep + 1] - stepPercentage[currentStep]

        val changeR = (stepColor[currentStep + 1].red - stepColor[currentStep].red) / stepLength
        val changeG = (stepColor[currentStep + 1].green - stepColor[currentStep].green) / stepLength
        val changeB = (stepColor[currentStep + 1].blue - stepColor[currentStep].blue) / stepLength


        for (i in stepPercentage[currentStep] until stepPercentage[currentStep + 1]) {
            val distanceFromStart = i - stepPercentage[currentStep]
            val interpolateR = stepColor[currentStep].red + changeR * distanceFromStart
            val interpolateG = stepColor[currentStep].green + changeG * distanceFromStart
            val interpolateB = stepColor[currentStep].blue + changeB * distanceFromStart

            colourArray[i] = Color(interpolateR, interpolateG, interpolateB)
        }
    }
    colourArray[100] = stepColor[stepColor.size - 1]
            //ie 0, 25, 50, 100
            //first step: i is 0 - 24
            //second step: i is 24 - 49
            //third step - i is 50 - 99
            //interpolation logic:
            // for each colour:
            // get colour difference between steps
            // divide by value difference between steps to get difference in colour for each step
            // multiply by how far into current step we are (i - stepPercentage[currentStep])
            // add to starting value for that colour
            //Log.d("i", i.toString())

    return colourArray
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
    roundCorners: Dp = 4.dp,
    width: Dp? = null,
    backgroundColor: Color? = null,
    textColor: Color? = null
) {
    Button(
        onClick = onClick,
        modifier = if (width != null) modifier.width(width) else modifier,
        shape = RoundedCornerShape(roundCorners),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor ?: ButtonDefaults.buttonColors().containerColor,
            contentColor = textColor ?: ButtonDefaults.buttonColors().contentColor
        )
    ) {
        Text(text = label)
    }
}
