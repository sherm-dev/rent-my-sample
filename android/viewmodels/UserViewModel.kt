package com.shermwebdev.rentmy.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shermwebdev.rentmy.data.User
import com.shermwebdev.rentmy.database.getDatabase
import com.shermwebdev.rentmy.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.IOException

class UserViewModel(application: Application): AndroidViewModel(application) {
    val repository = UserRepository(application.applicationContext)
    val database = getDatabase(application.applicationContext)

    fun getCurrentUser(userId: Int): Flow<User?>{
        return database.userDao().getUserById(userId)
    }

    fun refreshUser(userId: Int) = viewModelScope.launch{
        try{
            repository.getUser(userId)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun updateUserData(_user: User){
        if(_user.id == -1){
            addUser(_user)
        }else{
            updateUser(_user)
        }
    }

    private fun addUser(_user: User) = viewModelScope.launch {
        try{
            repository.addUser(_user)
        }catch(e: IOException){
            Log.e("ADD USER", e.message.toString())
            e.printStackTrace()
        }
    }

    private fun updateUser(_user: User) = viewModelScope.launch{
        try{
            repository.updateUser(_user)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }

    fun registerUser(email: String, password: String, token: String) = viewModelScope.launch{
        try{
            repository.registerUser(email, password, token)
        }catch(e: IOException){
            e.printStackTrace()
        }
    }
}