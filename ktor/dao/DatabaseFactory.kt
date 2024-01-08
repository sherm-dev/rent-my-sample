package com.shermwebdev.dao

import com.shermwebdev.models.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import org.jetbrains.exposed.sql.transactions.experimental.*

object DatabaseFactory {
    fun init() {
        val hikariConfig = HikariConfig()

        // The following URL is equivalent to setting the config options below:
        // jdbc:postgresql:///<DB_NAME>?cloudSqlInstance=<INSTANCE_CONNECTION_NAME>&
        // socketFactory=com.google.cloud.sql.postgres.SocketFactory&user=<DB_USER>&password=<DB_PASS>
        // See the link below for more info on building a JDBC URL for the Cloud SQL JDBC Socket Factory
        // https://github.com/GoogleCloudPlatform/cloud-sql-jdbc-socket-factory#creating-the-jdbc-url

        // Configure which instance and what database user to connect with.
        hikariConfig.jdbcUrl = String.format("jdbc:postgresql:///%s", "rentmy15")
        hikariConfig.username = "postgres" // e.g. "root", _postgres"
        hikariConfig.password = "my-password" // e.g. "my-password"
        hikariConfig.driverClassName = "org.postgresql.Driver"
        hikariConfig.maximumPoolSize = 10
        hikariConfig.isAutoCommit = false
        hikariConfig.transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        hikariConfig.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory")
        hikariConfig.addDataSourceProperty("cloudSqlInstance", "rent-my-blender:us-west2:rent-my-postgres-clone")
       // hikariConfig.addDataSourceProperty("enableIamAuth", true)

        // The ipTypes argument can be used to specify a comma delimited list of preferred IP types
        // for connecting to a Cloud SQL instance. The argument ipTypes=PRIVATE will force the
        // SocketFactory to connect with an instance's associated private IP.
        hikariConfig.addDataSourceProperty("ipTypes", "PUBLIC,PRIVATE")

        val database = Database.connect(HikariDataSource(hikariConfig))

        transaction(database){
            SchemaUtils.create(Customers)
            SchemaUtils.create(Vehicles)
            SchemaUtils.create(AppUserAddresses)
            SchemaUtils.create(VehicleAddresses)
            SchemaUtils.create(Licenses)
            SchemaUtils.create(Reservations)
            SchemaUtils.create(AppTransactions)
            SchemaUtils.create(VehiclePhotos)
            SchemaUtils.create(Countries)
            SchemaUtils.create(AddressStates)
            SchemaUtils.create(Passes)
            SchemaUtils.create(VehicleOptionsTable)
            SchemaUtils.create(VehicleMakes)
            SchemaUtils.create(VehicleModels)
            SchemaUtils.create(TempUsers)
            SchemaUtils.create(AppAuthTokens)
            SchemaUtils.create(FirebaseMessagingTokens)
            SchemaUtils.create(UserPhotos)
            SchemaUtils.create(DrivetrainOptions)
            SchemaUtils.create(EngineOptions)
            SchemaUtils.create(InductionOptions)
            SchemaUtils.create(Modifications)
            SchemaUtils.create(ModificationOptions)
            SchemaUtils.create(PowertrainConfigOptions)
            SchemaUtils.create(TransmissionOptions)
            SchemaUtils.create(VehicleRatings)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}