package com.shermwebdev.rentmy.ui.screens.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shermwebdev.rentmy.MainActivity
import com.shermwebdev.rentmy.data.Vehicle
import com.shermwebdev.rentmy.ui.screens.components.items.VehicleListItem

@Composable
fun VehicleListView(activity: MainActivity, vehicles: List<Vehicle>, onVehicleSelect: (Int) -> Unit){
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(400.dp)){
        LazyColumn{
            items(vehicles){
                VehicleListItem(
                    activity = activity,
                    vehicle = it,
                    onVehicleSelect = {vehicle: Vehicle ->
                    onVehicleSelect(vehicle.vehicleId)
                })
            }
        }
    }
}