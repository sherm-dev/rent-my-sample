package com.shermwebdev.rentmy.data

import androidx.room.*
import com.shermwebdev.data.*
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "vehicles",
    foreignKeys = arrayOf(
        ForeignKey(
            entity = User::class,
            parentColumns = arrayOf("app_user_id"),
            childColumns = arrayOf("vehicle_user_id")
        ),
        ForeignKey(
            entity = VehicleMake::class,
            parentColumns = arrayOf("vehicle_make_id"),
            childColumns = arrayOf("make")
        ),
        ForeignKey(
            entity = VehicleModel::class,
            parentColumns = arrayOf("vehicle_model_id"),
            childColumns = arrayOf("model")
        )
    )
)
data class Vehicle(
    @PrimaryKey
    @ColumnInfo(name = "vehicle_id")
    val vehicleId: Int = -1,
    @ColumnInfo(name = "make")
    val makeId: Int,
    @ColumnInfo(name = "model")
    val modelId: Int,
    @ColumnInfo(name = "price")
    val price: Int,
    @ColumnInfo(name = "year")
    val year: Int,
    @ColumnInfo(name = "plate")
    val plate: String,
    @ColumnInfo(name = "color")
    val color: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "vin")
    val vin: String,
    @ColumnInfo("vehicle_user_id")
    val userId: Int
)
