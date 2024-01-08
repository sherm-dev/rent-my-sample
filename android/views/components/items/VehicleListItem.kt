package com.shermwebdev.rentmy.ui.screens.components.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shermwebdev.rentmy.MainActivity
import com.shermwebdev.rentmy.data.Vehicle
import com.shermwebdev.rentmy.ui.screens.components.SecureImageView
import com.shermwebdev.rentmy.ui.viewmodels.VehicleOptionsViewModel
import com.shermwebdev.rentmy.ui.viewmodels.VehicleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehicleListItem(vehicle: Vehicle, onVehicleSelect: (Vehicle) -> Unit, activity: MainActivity){
    val vehicleViewModel = viewModel(VehicleViewModel::class.java)
    val vehicleOptionsViewModel = viewModel(VehicleOptionsViewModel::class.java)
    val mainImg = vehicleViewModel
        .selectedVehicleMainPhoto(vehicle.vehicleId)
        .collectAsState(null)

    val make = vehicleOptionsViewModel
        .makeById(vehicle.makeId)
        .collectAsState(null)

    val model = vehicleOptionsViewModel
        .modelById(vehicle.modelId)
        .collectAsState(null)


    Card(
        modifier = Modifier
            .fillMaxWidth(1f)
            .height(60.dp),
        onClick = {
            onVehicleSelect(vehicle)
        }
    ) {
        Row{
            if(mainImg.value != null)
                SecureImageView(bucket = mainImg.value?.bucket?: "", name = mainImg.value?.name?: "", activity = activity, modifier = null)

            Column(modifier = Modifier.align(Alignment.CenterVertically)){
                Text(
                    text = "${vehicle.year} ${make.value?.make} ${model.value?.model}"
                )

                Text(
                    text = vehicle.color
                )
            }
        }
    }
}