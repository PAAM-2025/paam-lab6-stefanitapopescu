package upt.paam.lab6

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import com.google.android.gms.location.LocationCallback
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.tasks.OnFailureListener
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import upt.paam.lab6.location.LocationHandler
import upt.paam.lab6.ui.theme.LAB6Theme


class MainActivity : ComponentActivity() {

    private val latLngState = mutableStateOf(LatLng(45.74745411062225, 21.226260284261446))
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isLocationPermissionGranted) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_ID
            )
        }

        enableEdgeToEdge()
        setContent {
            LAB6Theme {
                Column {
                    Row(
                        Modifier.weight(8f)
                    ) {
                        MapComposable()
                    }
                    Row(
                        Modifier
                            .weight(2f)
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                    ) {
                        LocationComposable()
                    }
                    // TODO 2: Add a button to call getCurrentLocation for retrieving current location
                    Button(onClick = { getCurrentLocation() }) {
                        Text("Get Current Location")
                    }
                }

            }
        }
    }


    @Composable
    private fun MapComposable() {
        var mapLoaded by remember { mutableStateOf(false) }
        val markerState = remember { MarkerState(latLngState.value) }
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(latLngState.value, 10f)
        }
        LaunchedEffect(latLngState.value) {
            if (!mapLoaded) return@LaunchedEffect
            cameraPositionState.animate(
                update = CameraUpdateFactory.newCameraPosition(
                    CameraPosition(latLngState.value, 15f, 0f, 0f)
                ),
                durationMs = 1000
            )
            markerState.position = latLngState.value
        }
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapLoaded = { mapLoaded = true }
        )
        {
            Marker(
                state = markerState,
                title = "One Marker"
            )
        }
        // TODO 1: Create a marker and set its position from [latLngState].
    }

    override fun onResume() {
        super.onResume()
        setupLocation()
        val handler = LocationHandler(this)
        handler.registerLocationListener(locationCallback!!)

    }


    private fun setupLocation() {
        this.locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    latLngState.value = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (isLocationPermissionGranted) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            // TODO 3 Add a fusedLocationClient function to retrieve the current location and set the marker to point to that location
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location: Location? ->
                location?.let {
                    latLngState.value = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    @Composable
    private fun LocationComposable() {
        Text(text = "Lat = ${latLngState.value.latitude}, Lng = ${latLngState.value.longitude}")
    }


    private val isLocationPermissionGranted: Boolean
        get() = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_ID = 111
    }

}

