package com.shermwebdev.rentmy.network

import android.content.Context
import android.util.Log
import com.shermwebdev.myapplication.R
import com.shermwebdev.rentmy.data.AppDisplayMessage
import com.shermwebdev.rentmy.data.AppDisplayMessageType
import com.shermwebdev.rentmy.data.AuthToken
import com.shermwebdev.rentmy.database.getDatabase
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

private val APPSERVER_BASE = "https://rent-my-app-server-test-e76v2nl57q-wl.a.run.app"


    //TODO: Do this in some other class?
    internal fun base64Encode(a: String, b: String): String = Base64.getEncoder().encodeToString("$a:$b".encodeToByteArray())

    internal fun createErrorMessage(errorCode: Int): String{
        return when(errorCode){
            401 -> "You are Unauthorized"
            500 -> "Internal Server Error"
            400 -> "That resource already exists"
            else -> ""
        }
    }

    object AppAPI {
        private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        internal fun retrofitService(context: Context): AppserverApiInterface{
            val okHttpClient = OkHttpClient.Builder()
                    //TODO: MyInterceptor
                .addNetworkInterceptor(Interceptor{
                    Log.i("REQUEST URL", "URL: ${it.request().url.host}")
                    return@Interceptor it.proceed(it.request())
                })
                .addInterceptor(Interceptor {
                    val request = it.request()
                    Log.i("REQUEST URL", "URL: ${request.url.host}")
                val builder = request.newBuilder().apply {


                    if (
                        request.method.equals("POST")
                        && (request.url.pathSegments[request.url.pathSegments.size - 1].equals(
                            "avatar"
                        )
                                || request.url.pathSegments[request.url.pathSegments.size - 1].equals(
                            "photos"
                        ))
                    ) {
                        header("Content-Type", "application/octet-stream")
                    } else {
                        header("Content-Type", "application/json;charset=utf-8")
                    }

                    if ((request.url.pathSegments[0].equals("users") && request.url.pathSegments.size == 1)
                        || request.url.pathSegments[request.url.pathSegments.size - 1].equals(
                            "auth_token"
                        )
                    ) {
                        val email = context.getSharedPreferences(
                            context.getString(R.string.preference_key),
                            Context.MODE_PRIVATE
                        ).getString(
                            context.getString(R.string.preference_email),
                            ""
                        )

                        val password = context.getSharedPreferences(
                            context.getString(R.string.preference_key),
                            Context.MODE_PRIVATE
                        ).getString(
                            context.getString(R.string.preference_password),
                            ""
                        )
                        Log.d("AUTHORIZATION", "Email: $email Password: $password")
                        Log.d(
                            "REQUEST ADD HEADER",
                            "HEADER: ${request.headers.toString()} BODY: ${request.body.toString()}"
                        )
                        addHeader(
                            "Authorization",
                            "Basic " + base64Encode(
                                email!!,
                                password!!
                            )
                        )
                    } else {
                        val authorization: AuthToken? = getDatabase(context).authTokenDao()
                            .getBearerAuthTokenForLoggedIn()
                        Log.i("Authorization", "BEARER: ${authorization?.token}")
                        addHeader(
                            "Authorization",
                            "Bearer " + (authorization?.token)
                        )
                    }

                }.build()

                val response = it.proceed(builder)
                Log.i("REQUEST INTERCEPTOR HTTP", "HEADER: " + request.headers.toString())
                //Log.i("HTTP REQUEST REQUEST", "HEaders: ${request.headers.toString()} BODY: ${request.body.toString()}")

                //  if(!response.isSuccessful) {

                Log.i("HTTP RESPONsE", response.toString())
                //TODO: Output Error Message and test from response
                if(response.code == 401 || response.code == 400 || response.code == 500) {
                    //output snackbar error
                    getDatabase(context).displayMessageDao().insertDisplayMessage(
                        AppDisplayMessage(
                            messageType = AppDisplayMessageType.MESSAGE_TYPE_ERROR,
                            displayMessage = createErrorMessage(response.code)
                        )
                    )
                    it.call().cancel()
                }

                if(response.code == 404)
                    it.call().cancel()
                //}





                return@Interceptor response
            })
            .build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(APPSERVER_BASE)
                .client(okHttpClient)
                .build()

            val retrofitService: AppserverApiInterface by lazy {
                retrofit.create(AppserverApiInterface::class.java)
            }

            return retrofitService
        }
    }
