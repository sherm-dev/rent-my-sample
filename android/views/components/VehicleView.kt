package com.shermwebdev.rentmy.ui.screens.components

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shermwebdev.rentmy.MainActivity
import com.shermwebdev.rentmy.ui.screens.components.items.VehicleListItem
import com.shermwebdev.rentmy.ui.screens.views.VehicleOptionsView
import com.shermwebdev.rentmy.ui.viewmodels.VehicleOptionsViewModel
import com.shermwebdev.rentmy.ui.viewmodels.VehicleViewModel

@Composable
fun VehicleView(vehicleId: Int?, activity: MainActivity){
    val vehicleViewModel = viewModel(VehicleViewModel::class.java)
    val vehicleOptionsViewModel = viewModel(VehicleOptionsViewModel::class.java)
    val vehicle = vehicleViewModel.selectedVehicle(vehicleId?: -1).collectAsState(null)
    val vehicleOptions = vehicleViewModel.selectedVehicleOptions(vehicle.value?.vehicleId?: -1).collectAsState(initial = null)
    val vehicleMods = vehicleViewModel.selectedVehicleModifications(vehicle.value?.vehicleId?: -1).collectAsState(initial = emptyList())
    val modOptions = vehicleOptionsViewModel.modificationOptions.observeAsState()


    LazyColumn{
        item{
            if(vehicle.value != null)
                VehicleListItem(vehicle = vehicle.value!!, onVehicleSelect = {}, activity = activity)
        }

        item{
            VehicleOptionsView(vehicleOptions = vehicleOptions.value)
        }

        item{
            VehicleModificationsView(modificationOptions = modOptions.value?: emptyList(), modifications = vehicleMods.value)
        }
    }

}