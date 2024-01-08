package com.shermwebdev.rentmy.repository

import android.content.Context
import android.util.Log
import com.shermwebdev.myapplication.R
import com.shermwebdev.rentmy.data.*
import com.shermwebdev.rentmy.database.CacheDatabase
import com.shermwebdev.rentmy.database.getDatabase
import com.shermwebdev.rentmy.datastore.DataStoreManager
import com.shermwebdev.rentmy.network.AppAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(val context: Context) {
    val database: CacheDatabase = getDatabase(context)
    val dataStoreManager = DataStoreManager(context)
    val sharedPreferences = context.getSharedPreferences(
        context.getString(R.string.preference_key),
        Context.MODE_PRIVATE
    )

    suspend fun refreshUserAddress(userId: Int){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).getUserAddress(userId)?.let{
                database.userDao().insertUserAddress(it)
            }
        }
    }

    suspend fun updateUserAddress(userId: Int, address: UserAddress){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).updateUserAddress(userId, address)?.let{
                database.userDao().insertUserAddress(it)
            }
        }
    }

    suspend fun addUserAddress(userId: Int, address: UserAddress){
        withContext(Dispatchers.IO){
            AppAPI.retrofitService(context).addUserAddress(userId, address)?.let{
                database.userDao().insertUserAddress(it)
            }
        }
    }

    suspend fun addUser(user: User) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addUser(user)?.let{
            Log.d("ADD USER", it.toString())
            context.getSharedPreferences(
                context.getString(R.string.preference_key),
                Context.MODE_PRIVATE
            )
            .edit()
            .putInt(
                context.getString(R.string.preference_user_id),
                it.id
            ).apply()

            dataStoreManager.updateUserIdPref(it.id)

            database.userDao().insertUser(it)
        }
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).updateUser(user.id, user)?.let{
            database.userDao().insertUser(it)
        }
    }

    suspend fun getUser(userId: Int) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getUser(userId)?.let {
            database.userDao().insertUser(it)
        }
    }

    suspend fun addUserPhoto(userId: Int, byteData: String) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addUserPhoto(
            userId,
            UserPhotoRequest(
                userPhotoId = -1,
                data = byteData,
                userId = userId,
                name = "avatar_${userId}.jpg"
            )
        )?.let{
            database.userDao().insertUserPhoto(it)
        }
    }

    suspend fun deleteUserPhoto(userId: Int, userPhoto: UserPhoto) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).deleteUserPhoto(userId, userPhoto.userPhotoId).let{
            if(it)
                database.userDao().deleteUserPhoto(userPhoto)
        }
    }

    suspend fun getUserPhoto(userId: Int) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getUserPhoto(userId)?.let{
            database.userDao().insertUserPhoto(it)
        }
    }

    suspend fun getUserLicense(userId: Int) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getUserLicense(userId).let{
            database.userDao().insertUserLicense(it)
        }
    }

    suspend fun addUserLicense(userId: Int, license: License) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addUserLicense(userId, license).let{
            database.userDao().insertUserLicense(it)
        }
    }

    suspend fun updateUserLicense(userId: Int, license: License) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).updateUserLicense(userId, license).let{
            database.userDao().insertUserLicense(it)
        }
    }

    suspend fun getUserLicensePhoto(userId: Int) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).getUserLicensePhoto(userId).let{
            database.userDao().insertUserLicensePhoto(it)
        }
    }

    suspend fun addUserLicensePhoto(userId: Int, photo: LicensePhoto) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).addUserLicensePhoto(userId, photo).let{
            database.userDao().insertUserLicensePhoto(it)
        }
    }

    suspend fun updateUserLicensePhoto(userId: Int, photo: LicensePhoto) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).updateUserLicensePhoto(userId, photo).let{
            database.userDao().insertUserLicensePhoto(it)
        }
    }

    suspend fun registerUser(email: String, password: String, token: String) = withContext(Dispatchers.IO){
        AppAPI.retrofitService(context).signupUser(
            TempUser(
                email = email,
                password = password,
                firebaseToken = token
            )
        ) //TRUE/FALSE returned and awaits message
    }
}