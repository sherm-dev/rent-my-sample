package com.shermwebdev.dao

import com.shermwebdev.data.AppUser
import com.shermwebdev.data.Password
import com.shermwebdev.data.UserAddress

interface UserFacade {
    suspend fun getUser(id: Int): AppUser?
    suspend fun getUserPasswordByEmail(email: String): Password?
    suspend fun addPassword(password: Password): Password?
    suspend fun addUser(appUser: AppUser): AppUser?
    suspend fun deleteUser(appUser: AppUser): Boolean
    suspend fun updateUser(appUser: AppUser): AppUser?
    suspend fun doesUserExist(email: String): Boolean
    suspend fun getUserAddress(userId: Int): UserAddress?
    suspend fun addUserAddress(address: UserAddress): UserAddress?
    suspend fun updateUserAddress(address: UserAddress): UserAddress?

    //TODO: REMOVE IN PRODUCTION
    suspend fun deleteUsers(): Boolean
}