package com.example.CDN.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.google.accompanist.permissions.*
import android.Manifest
import android.os.Build

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsHandler(onPermissionsGranted: () -> Unit) {
    val permissionsToRequest = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.READ_MEDIA_IMAGES)
            add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionState = rememberMultiplePermissionsState(permissionsToRequest)

    if (permissionState.allPermissionsGranted) {
        LaunchedEffect(Unit) {
            onPermissionsGranted()
        }
    } else {
        AlertDialog(
            onDismissRequest = { /* Prevent dismiss */ },
            title = { Text("RICHIESTA ACCESSO RETICOLO") },
            text = {
                Text("Il Clan richiede accesso a Posizione, Microfono e Galleria per il funzionamento dei nodi tattici.")
            },
            confirmButton = {
                Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                    Text("AUTORIZZA")
                }
            }
        )
    }
}
