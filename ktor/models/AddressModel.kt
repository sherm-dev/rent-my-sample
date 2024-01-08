package com.shermwebdev.models

import org.jetbrains.exposed.sql.Table

object AppUserAddresses: Table(){
    val userAddressId = integer("user_address_id").autoIncrement()
    val street = varchar("street", 256)
    val address2 = varchar("address_2", 55)
    val city = varchar("city", 128)
    val state = integer("state_id").references(AddressStates.stateId)
    val country = integer("country_id").references(Countries.countryId)
    val zipcode = varchar("zipcode", 12) //TODO: Double check zipcode length
    val externalId = varchar("external_id", 32)
    val userId = integer("user_id").references(Customers.id)
    override val primaryKey = PrimaryKey(userAddressId)
}