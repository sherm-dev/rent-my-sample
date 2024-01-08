package com.shermwebdev.rentmy.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shermwebdev.data.Modification
import com.shermwebdev.rentmy.data.*
import com.shermwebdev.rentmy.database.getDatabase
import com.shermwebdev.rentmy.repository.VehicleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.IOException

//TODO: ALL VIEW MODELS WITH FLOW - user, authtoken, states, countries, any others

class VehicleViewModel(application: Application): AndroidViewModel(application) {
    //TODO: Similar construct with live data pulling all rows and mutable live data for selected on Location View Model
    private val database = getDatabase(application.applicationContext)
    private val repository = VehicleRepository(application.applicationContext)

    fun selectedVehicle(vehicleId: Int): Flow<Vehicle>{
        return database.vehicleDao().getVehicleById(vehicleId)
    }

    fun selectedVehicleAddress(vehicleId: Int): Flow<VehicleAddress>{
        return database.vehicleDao().getVehicleAddress(vehicleId)
    }

    fun selectedVehicleModifications(vehicleId: Int): Flow<List<Modification>>{
        return database.vehicleDao().getVehicleModifications(vehicleId)
    }

    fun selectedVehicleMainPhoto(vehicleId: Int): Flow<VehiclePhoto>{
        return database.vehicleDao().getVehicleMainPhoto(vehicleId)
    }

    fun selectedVehiclePhotos(vehicleId: Int): Flow<List<VehiclePhoto>>{
        return database.vehicleDao().getVehiclePhotos(vehicleId)
    }

    fun selectedVehiclePhotosWithoutPrimary(vehicleId: Int): Flow<List<VehiclePhoto>>{
        return database.vehicleDao().getVehiclePhotosWithoutMain(vehicleId)
    }

    fun selectedVehicleOptions(vehicleId: Int): Flow<VehicleOptions>{
        return database.vehicleDao().getVehicleOptionsById(vehicleId)
    }

    fun vehicleAddressesByLocation(filters: VehicleResultFilter): Flow<List<VehicleAddress>>{
        return database.vehicleDao().getAddressesByLocationWithFilters(
            filters.toArgs()
        )
    }

    fun vehiclesByLocation(filters: VehicleResultFilter): Flow<List<Vehicle>>{

            return database.vehicleDao()
                .getVehiclesByLocationWithFilters(
                    filters.toArgs()
                )

    }

    fun vehiclesByUserId(userId: Int): Flow<List<Vehicle>>{
        return database.vehicleDao().getVehiclesByUserId(userId)
    }

    fun updateVehicleData(vehicleId: Int){
        refreshSelectedVehicle(vehicleId)
        refreshSelectedVehicleImage(vehicleId)
        refreshSelectedVehicleAddress(vehicleId)
        refreshModifications(vehicleId)
        refreshVehicleOptions(vehicleId)
        refreshVehiclePhotos(vehicleId)
    }

    private fun refreshModifications(vehicleId: Int) = viewModelScope.launch{
        try{
            repository.refreshVehicleModifications(vehicleId)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    private fun refreshSelectedVehicle(vehicleId: Int) = viewModelScope.launch{
        try{
            repository.refreshVehicleById(vehicleId)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    private fun refreshSelectedVehicleImage(vehicleId: Int) = viewModelScope.launch{
        try{
            repository.refreshVehicleMainPhoto(vehicleId)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun refreshVehiclesByLocation(coordinateWindow: CoordinateWindow) = viewModelScope.launch{
        try{
            repository.refreshVehiclesByLocation(coordinateWindow)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun refreshVehicleAddressesByLocation(coordinateWindow: CoordinateWindow) = viewModelScope.launch{
        try{
            repository.refreshVehicleAddressesByLocation(coordinateWindow)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    private fun refreshSelectedVehicleAddress(vehicleId: Int) = viewModelScope.launch {
        try{
            repository.refreshVehicleAddress(vehicleId)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    private fun refreshVehicleOptions(vehicleId: Int) = viewModelScope.launch{
        try{
            repository.refreshVehicleOptions(vehicleId)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun refreshVehiclePhotos(vehicleId: Int) = viewModelScope.launch{
        try{
            repository.refreshVehiclePhotos(vehicleId)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun addOrUpdateVehicleOptions(vehicleOptions: VehicleOptions){
        if(vehicleOptions.vehicleOptionsId == -1){
            addVehicleOptions(vehicleOptions)
        }else{
            updateVehicleOptions(vehicleOptions)
        }
    }

    private fun addVehicleOptions(vehicleOptions: VehicleOptions) = viewModelScope.launch{
        try{
            repository.addVehicleOptions(vehicleOptions)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    private fun updateVehicleOptions(vehicleOptions: VehicleOptions) = viewModelScope.launch{
        try{
            repository.updateVehicleOptions(vehicleOptions)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun createOrUpdateVehicle(vehicle: Vehicle){
        if(vehicle.vehicleId == -1){
            addVehicle(vehicle)
        }else{
            updateVehicle(vehicle)
        }
    }

    private fun addVehicle(vehicle: Vehicle) = viewModelScope.launch{
        try{
            repository.addVehicle(vehicle)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun deleteVehicle(vehicle: Vehicle) = viewModelScope.launch{
        try{
            repository.deleteVehicle(vehicle)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    private fun updateVehicle(vehicle: Vehicle) = viewModelScope.launch{
        try{
            repository.updateVehicle(vehicle)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }


    fun createOrUpdateVehicleAddress(vehicleAddress: VehicleAddress){
        if(vehicleAddress.vehicleAddressId == -1){
            addVehicleLocation(vehicleAddress)
        }else{
            updateVehicleLocation(vehicleAddress)
        }
    }

    fun updateVehicleLocation(address: VehicleAddress) = viewModelScope.launch{
        try{
            repository.updateVehicleAddress(address.vehicleId, address)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }


    fun addVehicleLocation(address: VehicleAddress) = viewModelScope.launch{
        try{
            repository.addVehicleAddress(address.vehicleId, address)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun addModifications(vehicleId: Int, modifications: List<Modification>) = viewModelScope.launch{
        try{
            repository.addVehicleModifications(vehicleId, modifications)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun removeModification(modification: Modification) = viewModelScope.launch{
        try{
            repository.deleteVehicleModification(modification)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun deleteVehiclePhoto(id: Int, vehiclePhoto: VehiclePhoto) = viewModelScope.launch{
        try{
            repository.deleteVehiclePhoto(id, vehiclePhoto)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun addVehiclePhoto(vehicleId: Int, byteData: String, primary: Boolean) = viewModelScope.launch{
        try{
            repository.addVehiclePhoto(vehicleId, byteData, primary)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun updateVehiclePhotos(vehicleId: Int, photos: List<VehiclePhoto>) = viewModelScope.launch{
        try{
            repository.updateVehiclePhotos(vehicleId, photos)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun addVehiclePhotos(vehicleId: Int, photos: List<VehiclePhotoRequest>) = viewModelScope.launch{
        try{
            repository.addVehiclePhotos(
                vehicleId,
                photos
            )
        }catch(e: IOException){
            e.printStackTrace()
        }
    }
}