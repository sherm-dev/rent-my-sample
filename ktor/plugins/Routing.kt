package com.shermwebdev.plugins

import com.shermwebdev.dao.DaoImpl
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import com.shermwebdev.routes.*
import io.ktor.http.*
import io.ktor.server.auth.*

fun Application.configureRouting() {
    routing {
        userRouting()
        vehicleRouting()
        locationRouting()
        reservationRouting()
        transactionRouting()
        makeModelRouting()
        braintreeRoute()
        signupRouting()
        vehicleOptionsRouting()
    }
}
