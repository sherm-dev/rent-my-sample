package com.shermwebdev.plugins

import com.shermwebdev.auth.JWTUtility
import com.shermwebdev.dao.DaoImpl
import com.shermwebdev.data.AppUser
import com.shermwebdev.data.AuthToken
import com.shermwebdev.logging.Logger
import io.fusionauth.jwt.domain.JWT
import io.github.aakira.napier.LogLevel
import io.github.aakira.napier.Napier
import io.ktor.server.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.sessions.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureSecurity() {


    authentication {
        val dao = DaoImpl()


        bearer("auth-bearer") {
            realm = "Access to the '/' path"
            authenticate { tokenCredential ->
                Napier.d(tag = "BEARER AUTH", message = "TOKEN CREDENTIAL: $tokenCredential")
                val jwtUtility = JWTUtility()
                val decodedToken: JWT = jwtUtility.decodeToken(tokenCredential.token)

                dao.getUser(decodedToken.subject.toInt()).let { appUser: AppUser? ->
                    if (appUser != null && jwtUtility.validateToken(
                            decodedToken,
                            appUser.username
                        )
                    ) {
                        //TODO: Should User Principal ID be email or should name in token be email?
                        UserIdPrincipal(appUser.email)
                    } else {
                        null
                    }
                }
            }
        }

        basic("auth-basic") {
            realm = "Access to the '/authenticate' path"
            validate { credentials ->
                Napier.log(priority = LogLevel.DEBUG, tag = "HTTP AUTH", null, message = credentials.toString())
                if(dao.doesUserExist(credentials.name)){
                    dao.getUserPasswordByEmail(credentials.name).let{
                        if(it != null && it.value == credentials.password)
                            UserIdPrincipal(credentials.name)
                        else
                            null
                    }
                }else{
                    dao.getTempUser(credentials.name).let{
                        Napier.log(priority = LogLevel.DEBUG, tag = "HTTP AUTH", null, message = "TEMP USER FOUND" + it?.password)
                        if(it != null && it.password == credentials.password)
                            UserIdPrincipal(it.email)
                        else
                            null
                    }
                }
            }
        }

        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                        name = "google",
                        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                        requestMethod = HttpMethod.Post,
                        clientId = System.getenv("GOOGLE_CLIENT_ID"),
                        clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                        defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
                )
            }
            client = HttpClient(Apache)
        }
    }
    routing {
        authenticate("auth-oauth-google") {
            get("login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/hello")
            }
        }
    }
}

class UserSession(accessToken: String)
