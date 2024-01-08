package com.shermwebdev.rentmy.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
@Entity(tableName = "app_users")
data class User(
    @PrimaryKey
    @ColumnInfo(name = "app_user_id")
    val id: Int = -1,
    @ColumnInfo(name = "firstname")
    val firstname: String,
    @ColumnInfo(name = "lastname")
    val lastname: String,
    @ColumnInfo(name = "username")
    val username: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "phone")
    val phone: String,
    @ColumnInfo(name = "customer_id")
    val customerId: String = ""
)
