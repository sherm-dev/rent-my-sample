package com.shermwebdev.data

import kotlinx.serialization.Serializable

@Serializable
data class UserAddress(
   val addressId: Int = -1,
   val street: String,
   val address2: String,
   val city: String,
   val stateId: Int,
   val countryId: Int,
   val zipcode: String,
   val userId: Int,
   val externalId: String = ""
)
