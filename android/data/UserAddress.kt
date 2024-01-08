package com.shermwebdev.rentmy.data

import androidx.room.*
import com.squareup.moshi.Json

@Entity(
    tableName = "user_addresses",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("app_user_id"),
            childColumns = arrayOf("user_address_owner_id")
        ),
        ForeignKey(
            entity = State::class,
            parentColumns = arrayOf("state_id"),
            childColumns = arrayOf("state_id")
        ),
        ForeignKey(
            entity = Country::class,
            parentColumns = arrayOf("country_id"),
            childColumns = arrayOf("country_id")
        ),
    )
)
data class UserAddress(
    @PrimaryKey
    @ColumnInfo(name = "user_address_id")
    val addressId: Int = -1,
    @Json(name = "street")
    @ColumnInfo(name = "street")
    val street: String,
    @Json(name = "address2")
    @ColumnInfo(name = "address2")
    val address2: String,
    @Json(name = "city")
    @ColumnInfo(name = "city")
    val city: String,
    @Json(name = "stateId")
    @ColumnInfo(name = "state_id")
    val stateId: Int,
    @Json(name = "countryId")
    @ColumnInfo(name = "country_id")
    val countryId: Int,
    @Json(name = "zipcode")
    @ColumnInfo(name = "zipcode")
    val zipcode: String,
    @Json(name = "userId")
    @ColumnInfo("user_address_owner_id")
    val userId: Int,
    @Json(name = "externalId")
    @ColumnInfo(name = "external_id")
    val externalId: String = ""
)
