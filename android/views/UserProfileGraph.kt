package com.shermwebdev.rentmy.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.shermwebdev.rentmy.MainActivity
import com.shermwebdev.rentmy.datastore.DataStoreManager
import com.shermwebdev.rentmy.ui.screens.views.UserProfileGroupUser
import com.shermwebdev.rentmy.ui.screens.views.UserProfileGroupUserAddress
import com.shermwebdev.rentmy.ui.screens.views.UserProfileGroupUserPhoto
import com.shermwebdev.rentmy.ui.screens.views.UserVehicleList
import com.shermwebdev.rentmy.ui.viewmodels.UserAddressViewModel
import com.shermwebdev.rentmy.ui.viewmodels.UserPhotoUploadViewModel
import com.shermwebdev.rentmy.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch

fun NavGraphBuilder.userProfileGraph(
    navController: NavController,
    activity: MainActivity,
    userId: Int?
){
    navigation(startDestination = "user_info", route = "profile"){
        composable("user_info"){
            val userViewModel = viewModel(UserViewModel::class.java)
            val userAddressViewModel = viewModel(UserAddressViewModel::class.java)
            val userPhotoViewModel = viewModel(UserPhotoUploadViewModel::class.java)
            val user = userViewModel.getCurrentUser(userId?: -1).collectAsState(initial = null)
            val userPhoto = userPhotoViewModel.getCurrentUserPhoto(userId?: -1).collectAsState(initial = null)
            val userAddress = userAddressViewModel.getCurrentUserAddress(userId?: -1).collectAsState(initial = null)

            Box(modifier = Modifier
                .fillMaxHeight(0.8f)
                .fillMaxWidth(1f)
                .padding(20.dp, 40.dp)) {
                LazyColumn(modifier = Modifier.paddingFromBaseline(30.dp, 30.dp)) {
                    item {
                        UserProfileGroupUserPhoto(userId = userId, userPhoto = userPhoto.value, activity = activity)
                    }

                    item {
                        UserProfileGroupUser(user.value, userPhoto.value)
                    }

                    item {
                        user.value?.let {
                            UserProfileGroupUserAddress(it.id, userAddress.value)
                        }
                    }

                    item{
                        val scope = rememberCoroutineScope()
                        UserVehicleList(
                            userId = userId,
                            onVehicleSelect = {
                                val dataStoreManager = DataStoreManager(activity.applicationContext)

                                scope.launch{
                                    dataStoreManager.updateVehicleIdPref(it)
                                }

                                navController.navigate("vehicle_edit_view")
                            },
                            onVehicleAdd = {
                                navController.navigate("vehicle")
                            },
                            activity = activity
                        )
                    }
                }
            }

        }
    }
}