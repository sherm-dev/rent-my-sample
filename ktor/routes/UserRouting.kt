package com.shermwebdev.routes

import com.shermwebdev.auth.JWTUtility
import com.shermwebdev.dao.DaoImpl
import com.shermwebdev.data.*
import com.shermwebdev.logging.Logger
import com.shermwebdev.payments.BraintreeUtility
import com.shermwebdev.data.UserPhotoRequest
import com.shermwebdev.storage.GoogleCloudStorageUtility
import io.github.aakira.napier.Napier
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import okhttp3.internal.http2.Header
import java.util.Base64

//TODO: ("Check if logged in user is the id being called")

fun emailPasswordFromHeader(headers: Headers): List<String>{
    var headerArray = emptyList<String>()
    val logger = Logger()

    logger.writeLog("HEADERS: ${headers.toString()}")
    if(headers.contains("Basic")){
        logger.writeLog("HEADERS: ${headers["Basic"]}")
        val decoded = Base64.getDecoder().decode(headers["Basic"]).decodeToString()
        Napier.d("DECODED: $decoded")
        headerArray = headerArray.plus(decoded.substring(0, decoded.indexOf(":")))
        headerArray = headerArray.plus(decoded.substring(decoded.indexOf(":") + 1))
    }

    return headerArray
}

fun Route.userRouting(){
    val dao = DaoImpl()


    route("users"){
        authenticate("auth-basic", strategy = AuthenticationStrategy.Required){
            post("{id}/auth_token"){
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                val id = call.parameters["id"] ?: return@post call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    if(id.toInt() > -1){
                        dao.getAuthTokenByUserId(id.toInt())?.apply{
                            call.respond(this)
                        }
                    }else{
                        call.respondText(
                            "Bad User ID",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            post{ //Add User
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                      "You Do Not Have Permission",
                      status = HttpStatusCode.Unauthorized
                )

                val appUser: AppUser = call.receive<AppUser>()
                val jwtUtility = JWTUtility()
                val btUtility = BraintreeUtility()
                val addedUser = AppUser(
                    id = appUser.id,
                    email = appUser.email,
                    firstname = appUser.firstname,
                    lastname= appUser.lastname,
                    username = appUser.username,
                    phone = appUser.phone,
                    customerId = btUtility.addCustomer(appUser)?: ""
                )

                val logger = Logger()
                // appUser.customerId = btUtility.addCustomer(appUser)?: ""
                //TODO: Do the same on vehicle address as I did here with btUtility - move lat/long location out of daoImpl
                try{
                    val headers = emailPasswordFromHeader(call.request.headers)
                    logger.writeLog("HEADERS: $headers")

                    dao.addUser(addedUser)?.apply{
                        val authToken = AuthToken(
                            token = jwtUtility.generateToken(addedUser.username, this.id),
                            userId = this.id
                        )

                        val password = Password(
                            userId = this.id,
                            id = -1,
                            value = headers[1]
                        )

                        dao.addPassword(password)
                        dao.addAuthToken(authToken)
                        call.respond(this) //return added user
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }
        }

        authenticate("auth-bearer", strategy = AuthenticationStrategy.Required){
            get("{id}"){
                call.principal<UserIdPrincipal>() ?: return@get call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    if(id.toInt() > -1){
                        val appUser: AppUser? = dao.getUser(id.toInt())
                        call.respond(appUser!!)
                    }else{
                        call.respondText(
                            "Bad User ID",
                            status = HttpStatusCode.BadRequest
                        )
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            post("{id}/address"){
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                val id = call.parameters["id"] ?: return@post call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val utility = BraintreeUtility()
                    val userAddress = call.receive<UserAddress>()
                    val user: AppUser? = dao.getUser(id.toInt())
                    val state: State = dao.getStateById(userAddress.stateId)
                    val country: Country = dao.getCountryById(userAddress.countryId)

                    val externalId: String =
                        if(user != null)
                            utility.addUserAddress(userAddress, state, country, user)?: ""
                        else
                            ""

                    val address = UserAddress(
                        addressId = userAddress.addressId,
                        street = userAddress.street,
                        address2 = userAddress.address2,
                        city = userAddress.city,
                        stateId = userAddress.stateId,
                        countryId = userAddress.countryId,
                        zipcode = userAddress.zipcode,
                        externalId = externalId,
                        userId = id.toInt()
                    )

                    dao.addUserAddress(address)?.apply{
                        call.respond(this)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            get("{id}/address"){
                call.principal<UserIdPrincipal>() ?: return@get call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    dao.getUserAddress(id.toInt())?.let{address: UserAddress ->
                        call.respond(address)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            put("{id}/address"){
                call.principal<UserIdPrincipal>() ?: return@put call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.Unauthorized
                )
                val id = call.parameters["id"] ?: return@put call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                val btUtility = BraintreeUtility()
                val userAddress: UserAddress

                try{
                    userAddress = call.receive<UserAddress>()
                    val state: State = dao.getStateById(userAddress.stateId)
                    val country: Country = dao.getCountryById(userAddress.countryId)
                    val appUser: AppUser? = dao.getUser(id.toInt())

                    appUser?.apply{
                        val updated: Boolean? = btUtility.updateUserAddress(userAddress, state, country, this)
                        if(updated != null && updated){
                            dao.updateUserAddress(userAddress)?.let{
                                call.respond(it)
                            }
                        }else{
                            call.respondText(
                                "Something went wrong, please try again.",
                                status = HttpStatusCode.BadRequest
                            )
                        }
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            get("{id}/vehicles"){
                call.principal<UserIdPrincipal>() ?: return@get call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val appVehicles: List<CustomerVehicle>? = dao.getVehiclesByUserId(id.toInt())
                    call.respond(appVehicles!!)
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            post("{id}/vehicles"){
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.Unauthorized
                )
                call.parameters["id"] ?: return@post call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val appVehicle = call.receive<CustomerVehicle>()
                    dao.addVehicle(appVehicle)?.let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            put("{id}/vehicles/{vehicleId}"){
                call.principal<UserIdPrincipal>() ?: return@put call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                call.parameters["id"] ?: return@put call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                call.parameters["vehicleId"] ?: return@put call.respondText(
                    "Missing Vehicle ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val appVehicle: CustomerVehicle = call.receive<CustomerVehicle>()
                    dao.updateVehicle(appVehicle)?.let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            delete("{id}/vehicles/{vehicleId}"){
                call.principal<UserIdPrincipal>() ?: return@delete call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                call.parameters["id"] ?: return@delete call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                val vehicleId = call.parameters["vehicleId"] ?: return@delete call.respondText(
                    "Missing Vehicle ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    dao.deleteVehicle(vehicleId.toInt()).let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            get("{id}/reservations"){
                call.principal<UserIdPrincipal>() ?: return@get call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val reservations: List<Reservation>? = dao.getUserReservations(id.toInt())
                    call.respond(reservations!!)
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            get("{id}/transactions"){
                call.principal<UserIdPrincipal>() ?: return@get call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val transactions: List<Transaction>? = dao.getUserTransactions(id.toInt())
                    call.respond(transactions!!)
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            put("{id}"){

                call.principal<UserIdPrincipal>() ?: return@put call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                call.parameters["id"] ?: return@put call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val appUser: AppUser = call.receive<AppUser>()
                    dao.updateUser(appUser)?.let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            delete("{id}"){
                call.principal<UserIdPrincipal>() ?: return@delete call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                call.parameters["id"] ?: return@delete call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    call.receive<AppUser>().let{
                        dao.deleteUser(it).let{deleted ->
                            call.respond(deleted)
                        }
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            get("{id}/license/photo"){
                call.principal<UserIdPrincipal>() ?: return@get call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                call.parameters["id"] ?: return@get call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                val licenseId = call.parameters["license_id"] ?: return@get call.respondText(
                    "Missing License ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    dao.getLicensePhoto(licenseId.toInt())?.let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            post("{id}/license/photo"){
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                call.parameters["id"] ?: return@post call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    dao.addLicensePhoto(call.receive<LicensePhoto>())?.let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            put("{id}/license/photo"){
                call.principal<UserIdPrincipal>() ?: return@put call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                call.parameters["id"] ?: return@put call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    dao.updateLicensePhoto(call.receive<LicensePhoto>())?.let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            get("{id}/avatar"){
                call.principal<UserIdPrincipal>() ?: return@get call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                val id = call.parameters["id"] ?: return@get call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    dao.getUserPhoto(id.toInt())?.let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            post("{id}/avatar"){
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.Unauthorized
                )

                val id = call.parameters["id"] ?: return@post call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val data: UserPhotoRequest = call.receive<UserPhotoRequest>()
                    val googleCloudStorageUtility = GoogleCloudStorageUtility()
                    val location: String = googleCloudStorageUtility.uploadImage(name = data.name, data = data.data)

                    val userPhoto = UserPhoto(
                        userPhotoId = data.userPhotoId,
                        userId = id.toInt(),
                        bucket = googleCloudStorageUtility.bucket.name,
                        name = data.name,
                        location = location
                    )

                    dao.addUserPhoto(userPhoto)?.let{userPhoto: UserPhoto ->
                        call.respond(userPhoto)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            delete("{id}/avatar/{photoId}"){
                call.principal<UserIdPrincipal>() ?: return@delete call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                val id = call.parameters["id"] ?: return@delete call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val googleCloudStorageUtility = GoogleCloudStorageUtility()
                    val photoId = call.parameters["photoId"] ?: return@delete call.respondText(
                        "Missing Photo ID",
                        status = HttpStatusCode.BadRequest
                    )

                    dao.getUserPhoto(id.toInt())?.let{
                        googleCloudStorageUtility.deletePhoto(
                            it.bucket,
                            it.name
                        )
                    }

                    dao.deleteUserPhoto(photoId.toInt()).let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            post("{id}/message_token"){
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                call.parameters["id"] ?: return@post call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val token = call.receive<FirebaseMessagingToken>()
                    dao.addFirebaseToken(token).let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }

            put("{id}/message_token"){
                call.principal<UserIdPrincipal>() ?: return@put call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )
                call.parameters["id"] ?: return@put call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                try{
                    val token = call.receive<FirebaseMessagingToken>()
                    dao.updateFirebaseToken(token).let{
                        call.respond(it)
                    }
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }



            /*post("{id}/auth_token"){
                call.principal<UserIdPrincipal>() ?: return@post call.respondText(
                    "You Do Not Have Permission",
                    status = HttpStatusCode.BadRequest
                )

                val jwtUtility = JWTUtility()
                val id = call.parameters["id"] ?: return@post call.respondText(
                    "Missing User ID",
                    status = HttpStatusCode.BadRequest
                )

                val authToken = call.receive<AuthToken>()

                dao.getUser(id.toInt()).let{user: User? ->
                    if(user != null){
                        dao.updateAuthToken(
                            AuthToken(
                                id = authToken.id,
                                userId = authToken.userId,
                                token = jwtUtility.generateToken(user.username, user.id)
                            )
                        ).let{
                            call.respond(it!!)
                        }
                    }
                }
            }*/
        }
    }
}