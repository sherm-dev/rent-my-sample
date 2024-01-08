package com.shermwebdev.models


import org.jetbrains.exposed.sql.Table

object Customers: Table(){
    val id = integer("id").autoIncrement()
    val firstname = varchar("firstname", 55)
    val lastname  = varchar("lastname", 128)
    val username = varchar("username", 30)
    val email = varchar("email", 128)
    val phone = varchar("phone", 16) //TODO: Check phone number length
    val customerId = varchar("customer_id", 32) //Braintree or other payment gateway id

    override val primaryKey = PrimaryKey(id)
}