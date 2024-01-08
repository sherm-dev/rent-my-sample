package com.shermwebdev.data

import kotlinx.serialization.Serializable

@Serializable
data class AppUser(
    val id: Int = -1,
    val firstname: String,
    val lastname: String,
    val username: String,
    val email: String,
    val phone: String,
    val customerId: String = ""
)
