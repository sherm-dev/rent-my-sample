package com.shermwebdev.rentmy.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.shermwebdev.myapplication.R
import com.shermwebdev.rentmy.MainActivity
import com.shermwebdev.rentmy.data.CoordinateWindow
import com.shermwebdev.rentmy.data.Vehicle
import com.shermwebdev.rentmy.data.VehicleResultFilter
import com.shermwebdev.rentmy.ui.screens.components.FilterDialog
import com.shermwebdev.rentmy.ui.screens.components.VehicleListView
import com.shermwebdev.rentmy.ui.screens.components.VehicleView
import com.shermwebdev.rentmy.ui.screens.components.forms.fields.*
import com.shermwebdev.rentmy.ui.viewmodels.VehicleViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Map(
    currentLocation: LatLng,
    onMarkerClick: (Int) -> Unit,
    selectedVehicle: Vehicle?,
    activity: MainActivity,
    isList: Boolean,
    isFilter: Boolean,
    onFilterDismiss: () -> Unit,
    onVehicleView: (Int) -> Unit
){
    val defaultFilters = VehicleResultFilter(
        year = null,
        makeId = null,
        modelId = null,
        drivetrainId = null,
        engineId = null,
        powertrainConfigId = null,
        inductionId = null,
        transmissionId = null,
        color = null,
        latitude = currentLocation.latitude,
        longitude = currentLocation.longitude
    );

    val vehicleViewModel = viewModel(VehicleViewModel::class.java)

    val sheetState = rememberModalBottomSheetState()
    val (userClosed, setUserClosed) = remember{
        mutableStateOf(false)
    }
    
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 10f)
    }

    val (searchFilter, setSearchFilter) = remember{
        mutableStateOf<VehicleResultFilter>(
            defaultFilters
        )
    }

    val (mapLocation, setMapLocation) = remember{
        mutableStateOf(currentLocation)
    }

    Log.i("MAP LOCATION", mapLocation.toString())

    val onVehicleSelect = { id: Int ->
        setUserClosed(false)
        onMarkerClick(id) //sets vehicle id and refreshes vehicle
    }

    val onMapClick = {latLng: LatLng ->
        setMapLocation(LatLng(latLng.latitude, latLng.longitude))
        setSearchFilter(
            VehicleResultFilter(
                searchFilter.year,
                searchFilter.makeId,
                searchFilter.modelId,
                searchFilter.drivetrainId,
                searchFilter.engineId,
                searchFilter.powertrainConfigId,
                searchFilter.inductionId,
                searchFilter.transmissionId,
                searchFilter.color,
                latLng.latitude,
                latLng.longitude
            )
        )
        vehicleViewModel.refreshVehiclesByLocation(
            CoordinateWindow(
                latLng.longitude -1,
                latLng.latitude + 1,
                latLng.longitude -1,
                latLng.latitude -1
            )
        )

        vehicleViewModel.refreshVehicleAddressesByLocation(
            CoordinateWindow(
                latLng.longitude -1,
                latLng.latitude + 1,
                latLng.longitude -1,
                latLng.latitude -1
            )
        )
    }


    if(!isList){
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMyLocationClick = {
                setMapLocation(LatLng(it.latitude, it.longitude))
            },
            onMapLongClick = {
                setMapLocation(LatLng(it.latitude, it.longitude)) //TODO: Debug this and onMyLocationClick - not setting new location - vehicle addresses should change with mapLocation
            },
            onMapClick = {
                onMapClick(it)
            }
        ) {
            val vehicleAddresses = vehicleViewModel.vehicleAddressesByLocation(searchFilter).collectAsState(emptyList())
            Log.i("VEHICLE LIST", mapLocation.toString())
            Log.i("VEHICLE LIST", vehicleAddresses.value.toString())

            Circle(center = mapLocation, radius = 160000.0, visible = true, strokeWidth = 1f, strokeColor = Color.Red) //CHange radius in settings and adjust coordinateWindow to match

            vehicleAddresses.value.forEach { vehicleAddress ->
                val markerState = rememberMarkerState(position = LatLng(vehicleAddress.lat, vehicleAddress.longitude))
                Marker(
                    state = markerState,
                    title = "",
                    snippet = stringResource(R.string.google_maps_marker_snippet),
                    onClick = {
                        onVehicleSelect(vehicleAddress.vehicleId)
                        Log.i("MARKER CLICK", it.toString())
                        return@Marker true
                    }
                )
            }
        }
    }else{
        val vehicles = vehicleViewModel.vehiclesByLocation(searchFilter).collectAsState(emptyList())
        VehicleListView(
            activity = activity,
            vehicles = vehicles.value,
            onVehicleSelect = {
                onVehicleSelect(it)
            }
        )
    }
    
    if(isFilter){
        FilterDialog(
            resultFilter = searchFilter,
            onFilterDismiss = onFilterDismiss,
            onFilterSubmit = {
                Log.i("VEHICLE LIST", it.toString())
                setSearchFilter(it)
                onFilterDismiss()
            },
            onClearFilters = {
                setSearchFilter(defaultFilters)
            },
            currentLocation = mapLocation //filter where they've set location
        )
    }

    if(selectedVehicle != null && !userClosed){
        ModalBottomSheet(
            onDismissRequest = {
                setUserClosed(true)
                onMarkerClick(-1) //set selected vehicle to -1
            },
            sheetState = sheetState
        ){
            Box(modifier = Modifier
                .fillMaxWidth(1f)
                .height(400.dp)){
                VehicleView(vehicleId = selectedVehicle.vehicleId, activity = activity)
                Button(
                    modifier = Modifier.align(Alignment.TopEnd),
                    onClick = {
                        onVehicleView(selectedVehicle.vehicleId)
                    }
                ){
                    Text(text = stringResource(R.string.button_label_vehicle_details))
                }
            }
        }
    }
}