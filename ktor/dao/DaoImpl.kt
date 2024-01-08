package com.shermwebdev.dao

import com.braintreegateway.PaymentMethodNonce
import com.shermwebdev.dao.DatabaseFactory.dbQuery
import com.shermwebdev.data.*
import com.shermwebdev.data.Transaction
import com.shermwebdev.logging.Logger
import com.shermwebdev.models.*
import com.shermwebdev.payments.BraintreeUtility
import com.shermwebdev.rentmy.data.VehicleOptions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq

class DaoImpl:
    UserFacade,
    VehicleFacade,
    LocationFacade,
    LicenseFacade,
    PhotoFacade,
    TransactionFacade,
    ReservationFacade,
    TempFacade,
    FirebaseMessagingTokenFacade,
    AuthTokenFacade{
    override suspend fun getUser(id: Int): AppUser? = dbQuery {
        Customers.select(Customers.id eq id).map(::resultToUser).singleOrNull()
    }

    override suspend fun getUserPasswordByEmail(email: String): Password? = dbQuery{
        (Customers innerJoin Passes)
            .slice(
                Customers.id,
                Customers.email,
                Passes.userId,
                Passes.passwordId,
                Passes.value
            )
            .select(Customers.id eq Passes.userId)
            .andWhere{Customers.email eq email}
            .singleOrNull()
            ?.let(::resultToPassword)
    }

    override suspend fun addPassword(password: Password): Password? = dbQuery{
        Passes.insert {
            it[Passes.userId] = password.userId
            it[Passes.value] = password.value
        }.resultedValues?.singleOrNull()?.let(::resultToPassword)
    }

    override suspend fun addUser(appUser: AppUser): AppUser? = dbQuery{ //TODO: Add date created
        Customers.insert{
            it[Customers.firstname] = appUser.firstname
            it[Customers.lastname] = appUser.lastname
            it[Customers.email] = appUser.email
            it[Customers.phone] = appUser.phone
            it[Customers.username] = appUser.username
            it[Customers.customerId] = appUser.customerId
        }.resultedValues?.singleOrNull()?.let(::resultToUser)
    }

    suspend fun getTempUsers(): List<TempUser> = dbQuery{
        TempUsers.selectAll().map(::resultToTempUser)
    }

    suspend fun getAllUsers(): List<AppUser> = dbQuery{
        TempUsers.selectAll().map(::resultToUser)
    }

    override suspend fun deleteUser(appUser: AppUser): Boolean = dbQuery{
        val utility = BraintreeUtility()
        Customers.deleteWhere{Customers.id eq appUser.id} > 0 && utility.deleteCustomer(appUser.customerId)
    }

    override suspend fun updateUser(appUser: AppUser): AppUser? = dbQuery{
        if(Customers.update({Customers.id eq appUser.id}){
            it[Customers.id] = appUser.id
            it[Customers.firstname] = appUser.firstname
            it[Customers.lastname] = appUser.lastname
            it[Customers.username] = appUser.username
            it[Customers.email] = appUser.email
            it[Customers.phone] = appUser.phone
            it[Customers.customerId] = appUser.customerId
        } > 0)
            appUser
        else
            null
    }

    override suspend fun doesUserExist(email: String): Boolean = dbQuery{
        Customers.select(Customers.email eq email).count() > 0
    }

    override suspend fun getVehicle(id: Int): CustomerVehicle? = dbQuery{
        Vehicles.select(Vehicles.vehicleId eq id).map(::resultToVehicle).singleOrNull()
    }

    override suspend fun addVehicle(appVehicle: CustomerVehicle): CustomerVehicle? = dbQuery{
        val insert = Vehicles.insert{
            it[Vehicles.makeId] = appVehicle.makeId
            it[Vehicles.modelId] = appVehicle.modelId
            it[Vehicles.price] = appVehicle.price
            it[Vehicles.year] = appVehicle.year
            it[Vehicles.plate] = appVehicle.plate
            it[Vehicles.color] = appVehicle.color
            it[Vehicles.vin] = appVehicle.vin
            it[Vehicles.description] = appVehicle.description
            it[Vehicles.userId] = appVehicle.userId
        }

        insert.resultedValues?.singleOrNull()?.let(::resultToVehicle)
    }

    override suspend fun deleteVehicle(id: Int): Boolean = dbQuery{
        Vehicles.deleteWhere{Vehicles.vehicleId eq id} > 0
    }

    override suspend fun updateVehicle(appVehicle: CustomerVehicle): CustomerVehicle? = dbQuery{
        if(Vehicles.update({Vehicles.vehicleId eq appVehicle.vehicleId}){
            it[Vehicles.vehicleId] = appVehicle.vehicleId
            it[Vehicles.makeId] = appVehicle.makeId
            it[Vehicles.modelId] = appVehicle.modelId
            it[Vehicles.price] = appVehicle.price
            it[Vehicles.year] = appVehicle.year
            it[Vehicles.plate] = appVehicle.plate
            it[Vehicles.color] = appVehicle.color
            it[Vehicles.vin] = appVehicle.vin
            it[Vehicles.description] = appVehicle.description
            it[Vehicles.userId] = appVehicle.userId
        } > 0)
            appVehicle
        else
            null
    }

    override suspend fun getVehiclesByLocation(coordinateWindow: CoordinateWindow): List<CustomerVehicle> = dbQuery{
            (Vehicles innerJoin VehicleAddresses)
                .slice(
                    Vehicles.vehicleId,
                    Vehicles.makeId,
                    Vehicles.modelId,
                    Vehicles.year,
                    Vehicles.plate,
                    Vehicles.color,
                    Vehicles.description,
                    Vehicles.vin,
                    Vehicles.userId,
                    VehicleAddresses.vehicleId,
                    VehicleAddresses.lat,
                    VehicleAddresses.long,
                )
                .select(VehicleAddresses.vehicleId eq Vehicles.vehicleId)
                .andWhere{VehicleAddresses.lat lessEq coordinateWindow.t}
                .andWhere { VehicleAddresses.lat greaterEq coordinateWindow.b }
                .andWhere { VehicleAddresses.long lessEq coordinateWindow.r }
                .andWhere { VehicleAddresses.long greaterEq coordinateWindow.l }
                .map(::resultToVehicle)
    }

    override suspend fun getVehicleAddressesByLocation(coordinateWindow: CoordinateWindow): List<AppVehicleAddress> = dbQuery{
        VehicleAddresses.select(VehicleAddresses.lat lessEq coordinateWindow.t)
            .andWhere { VehicleAddresses.lat greaterEq coordinateWindow.b }
            .andWhere { VehicleAddresses.long lessEq coordinateWindow.r }
            .andWhere { VehicleAddresses.long greaterEq coordinateWindow.l }
            .map(::resultToVehicleAddress)
    }

    override suspend fun getVehiclesByUserId(userId: Int): List<CustomerVehicle>? = dbQuery{
        Vehicles.select(Vehicles.userId eq userId).map(::resultToVehicle)
    }

    override suspend fun addUserAddress(address: UserAddress): UserAddress? = dbQuery{
        val insert = AppUserAddresses.insert{
            it[AppUserAddresses.street] = address.street
            it[AppUserAddresses.address2] = address.address2
            it[AppUserAddresses.city] = address.city
            it[AppUserAddresses.state] = address.stateId
            it[AppUserAddresses.country] = address.countryId
            it[AppUserAddresses.zipcode] = address.zipcode
            it[AppUserAddresses.userId] = address.userId
            it[AppUserAddresses.externalId] = address.externalId
        }

        val logger = Logger()
        logger.writeLog("INSERT VAR $insert")

        logger.writeLog("INSERT RESULT " + insert.resultedValues?.toString())

        insert.resultedValues?.singleOrNull()?.let(::resultToUserAddress)
    }

    override suspend fun getVehicleAddress(vehicleId: Int): AppVehicleAddress? = dbQuery{
        (VehicleAddresses innerJoin AddressStates innerJoin Countries)
            .slice(
                VehicleAddresses.street,
                VehicleAddresses.city, 
                VehicleAddresses.state, 
                VehicleAddresses.country, 
                VehicleAddresses.zipcode,
                VehicleAddresses.vehicleId,
                VehicleAddresses.lat, 
                VehicleAddresses.long,
                AddressStates.stateId,
                AddressStates.name,
                AddressStates.code,
                AddressStates.parent,
                Countries.countryId,
                Countries.name,
                Countries.code
            )
            .select(VehicleAddresses.vehicleId eq vehicleId)
            .andWhere { VehicleAddresses.state eq AddressStates.stateId }
            .andWhere { VehicleAddresses.country eq Countries.countryId }
            .map(::resultToVehicleAddress)
            .singleOrNull()
    }

    override suspend fun getUserAddress(userId: Int): UserAddress? = dbQuery{
        (AppUserAddresses innerJoin AddressStates innerJoin Countries)
            .slice(
                AppUserAddresses.userAddressId,
                AppUserAddresses.street,
                AppUserAddresses.address2,
                AppUserAddresses.city,
                AppUserAddresses.state,
                AppUserAddresses.country,
                AppUserAddresses.zipcode,
                AppUserAddresses.externalId,
                AddressStates.stateId,
                Countries.countryId
            )
            .select(AppUserAddresses.userId eq userId)
            .andWhere { AppUserAddresses.state eq AddressStates.stateId }
            .andWhere { AppUserAddresses.country eq Countries.countryId }
            .map(::resultToUserAddress)
            .singleOrNull()
    }

    override suspend fun updateUserAddress(address: UserAddress): UserAddress? = dbQuery{
        if(AppUserAddresses.update({AppUserAddresses.userAddressId eq address.addressId}){
            it[AppUserAddresses.userAddressId] = address.addressId
            it[AppUserAddresses.street] = address.street
            it[AppUserAddresses.address2] = address.address2
            it[AppUserAddresses.city] = address.city
            it[AppUserAddresses.state] = address.stateId
            it[AppUserAddresses.country] = address.countryId
            it[AppUserAddresses.zipcode] = address.zipcode
            it[AppUserAddresses.externalId] = address.externalId
            it[AppUserAddresses.userId] = address.userId
        } > 0)
            address
        else
            null
    }

    override suspend fun deleteUsers(): Boolean = dbQuery{
        Customers.deleteAll() > 0
    }

    override suspend fun addVehicleAddress(address: AppVehicleAddress): AppVehicleAddress? = dbQuery{
        //val utility = GoogleLocationUtility()
      //  val validated: Boolean = utility.validateAddress(address)

        //if(validated){
            VehicleAddresses.insert{
                it[VehicleAddresses.street] = address.street
                it[VehicleAddresses.city] = address.city
                it[VehicleAddresses.state] = address.stateId
                it[VehicleAddresses.country] = address.countryId
                it[VehicleAddresses.zipcode] = address.zipcode
                it[VehicleAddresses.vehicleId] = address.vehicleId
                it[VehicleAddresses.lat] = address.lat
                it[VehicleAddresses.long] = address.long
            }.resultedValues?.singleOrNull()?.let(::resultToVehicleAddress)
        //}else{
          //  null
        //}
    }

    override suspend fun updateVehicleAddress(address: AppVehicleAddress): AppVehicleAddress? = dbQuery{
       // val utility = GoogleLocationUtility()
        //val validated: Boolean = utility.validateAddress(address)
//TODO: VALIDATE?
      //  if(validated){
            if(VehicleAddresses.update({VehicleAddresses.vehicleAddressId eq address.vehicleAddressId}){
                it[VehicleAddresses.vehicleAddressId] = address.vehicleAddressId
                it[VehicleAddresses.street] = address.street
                it[VehicleAddresses.city] = address.city
                it[VehicleAddresses.state] = address.stateId
                it[VehicleAddresses.country] = address.countryId
                it[VehicleAddresses.zipcode] = address.zipcode
                it[VehicleAddresses.vehicleId] = address.vehicleId
                it[VehicleAddresses.lat] = address.lat
                it[VehicleAddresses.long] = address.long
            } > 0)
                address
            else
                null
        //}

        //validated//return validated if false
    }

    override suspend fun addModification(modification: Modification): Modification? = dbQuery{
        Modifications.insert{
            it[Modifications.modTypeId] = modification.modTypeId
            it[Modifications.modVehicleId] = modification.modVehicleId
            it[Modifications.input] = modification.input
        }.resultedValues?.singleOrNull()?.let(::resultToModification)
    }

    override suspend fun updateModification(modification: Modification): Modification? = dbQuery{
        Modifications.upsert{
            it[Modifications.modId] = modification.modId
            it[Modifications.modTypeId] = modification.modTypeId
            it[Modifications.modVehicleId] = modification.modVehicleId
            it[Modifications.input] = modification.input
        }.resultedValues?.singleOrNull()?.let(::resultToModification)
    }

    override suspend fun deleteModification(modificationId: Int): Boolean = dbQuery{
        Modifications.deleteWhere{ Modifications.modId eq modificationId } > 0
    }

    override suspend fun getModification(modificationId: Int): Modification? = dbQuery{
        Modifications.select(Modifications.modId eq modificationId).singleOrNull()?.let(::resultToModification)
    }

    override suspend fun getModificationsByVehicleId(vehicleId: Int): List<Modification> = dbQuery{
        (Modifications innerJoin Vehicles)
            .slice(
                Modifications.modId,
                Modifications.modTypeId,
                Modifications.modVehicleId,
                Modifications.input,
                Vehicles.vehicleId
            )
            .select(
                Modifications.modVehicleId eq Vehicles.vehicleId
            )
            .andWhere { Vehicles.vehicleId eq vehicleId }
            .map(::resultToModification)
    }

    override suspend fun countModificationOptions(): Int = dbQuery{
        ModificationOptions.selectAll().count().toInt()
    }

    override suspend fun countInductionOptions(): Int = dbQuery{
        InductionOptions.selectAll().count().toInt()
    }

    override suspend fun countDrivetrainOptions(): Int = dbQuery{
        DrivetrainOptions.selectAll().count().toInt()
    }

    override suspend fun countTransmissionOptions(): Int = dbQuery{
        TransmissionOptions.selectAll().count().toInt()
    }

    override suspend fun countPowertrainConfigOptions(): Int = dbQuery{
        PowertrainConfigOptions.selectAll().count().toInt()
    }

    override suspend fun countEngineOptions(): Int = dbQuery{
        EngineOptions.selectAll().count().toInt()
    }

    override suspend fun getVehicleOptions(vehicleId: Int): VehicleOptions? = dbQuery{
        VehicleOptionsTable
            .select(VehicleOptionsTable.vehicleOptionsVehicleId eq vehicleId)
            .singleOrNull()
            ?.let(::resultToVehicleOptions)
    }

    override suspend fun updateVehicleOptions(vehicleOptions: VehicleOptions): VehicleOptions? = dbQuery{
        if(VehicleOptionsTable.update{
                it[VehicleOptionsTable.vehicleOptionsId] = vehicleOptions.vehicleOptionsId
                it[VehicleOptionsTable.drivetrainOptionId] = vehicleOptions.drivetrainOptionId
                it[VehicleOptionsTable.engineOptionId] = vehicleOptions.engineOptionId
                it[VehicleOptionsTable.inductionOptionId] = vehicleOptions.inductionOptionId
                it[VehicleOptionsTable.powertrainConfigOptionId] = vehicleOptions.powertrainConfigId
                it[VehicleOptionsTable.transmissionOptionId] = vehicleOptions.transmissionOptionId
                it[VehicleOptionsTable.vehicleOptionsVehicleId] = vehicleOptions.vehicleOptionsVehicleId
            } > 0)
            vehicleOptions
        else
            null
    }

    override suspend fun addVehicleOptions(vehicleOptions: VehicleOptions): VehicleOptions? = dbQuery{
        VehicleOptionsTable.insert{
            it[VehicleOptionsTable.drivetrainOptionId] = vehicleOptions.drivetrainOptionId
            it[VehicleOptionsTable.engineOptionId] = vehicleOptions.engineOptionId
            it[VehicleOptionsTable.inductionOptionId] = vehicleOptions.inductionOptionId
            it[VehicleOptionsTable.powertrainConfigOptionId] = vehicleOptions.powertrainConfigId
            it[VehicleOptionsTable.transmissionOptionId] = vehicleOptions.transmissionOptionId
            it[VehicleOptionsTable.vehicleOptionsVehicleId] = vehicleOptions.vehicleOptionsVehicleId
        }.resultedValues?.singleOrNull()?.let(::resultToVehicleOptions)
    }

    override suspend fun deleteVehicleOptions(vehicleOptionsId: Int): Boolean = dbQuery{
        VehicleOptionsTable.deleteWhere { VehicleOptionsTable.vehicleOptionsId eq vehicleOptionsId } > 0
    }

    override suspend fun getModificationOptions(): List<ModificationOption> = dbQuery{
        ModificationOptions.selectAll().map(::resultToModificationOption)
    }

    override suspend fun getInductionOptions(): List<InductionOption> = dbQuery{
        InductionOptions.selectAll().map(::resultToInductionOption)
    }

    override suspend fun getDrivetrainOptions(): List<DrivetrainOption> = dbQuery{
        DrivetrainOptions.selectAll().map(::resultToDrivetrainOption)
    }

    override suspend fun getTransmissionOptions(): List<TransmissionOption> = dbQuery{
        TransmissionOptions.selectAll().map(::resultToTransmissionOption)
    }

    override suspend fun getPowertrainConfigOptions(): List<PowertrainConfigOption> = dbQuery{
        PowertrainConfigOptions.selectAll().map(::resultToPowertrainConfigOption)
    }

    override suspend fun getEngineOptions(): List<EngineOption> = dbQuery{
        EngineOptions.selectAll().map(::resultToEngineOption)
    }

    override suspend fun getModificationOption(modificationOptionId: Int): ModificationOption = dbQuery{
        ModificationOptions.select(ModificationOptions.modOptionId eq modificationOptionId)
            .single()
            .let(::resultToModificationOption)
    }

    override suspend fun getInductionOption(inductionOptionId: Int): InductionOption = dbQuery{
        InductionOptions
            .select(InductionOptions.inductionOptionId eq inductionOptionId)
            .single()
            .let(::resultToInductionOption)
    }

    override suspend fun getDrivetrainOption(drivetrainOptionId: Int): DrivetrainOption = dbQuery{
        DrivetrainOptions.select(DrivetrainOptions.drivetrainOptionId eq drivetrainOptionId)
            .single()
            .let(::resultToDrivetrainOption)
    }

    override suspend fun getTransmissionOption(transmissionOptionId: Int): TransmissionOption = dbQuery{
        TransmissionOptions.select(TransmissionOptions.transmissionOptionId eq transmissionOptionId)
            .single()
            .let(::resultToTransmissionOption)
    }

    override suspend fun getPowertrainconfigOption(powertrainConfigOptionId: Int): PowertrainConfigOption = dbQuery{
        PowertrainConfigOptions.select(PowertrainConfigOptions.powertrainConfigOptionId eq powertrainConfigOptionId)
            .single()
            .let(::resultToPowertrainConfigOption)
    }

    override suspend fun getEngineOption(engineOptionId: Int): EngineOption = dbQuery{
        EngineOptions.select(EngineOptions.engineOptionId eq engineOptionId)
            .single()
            .let(::resultToEngineOption)
    }

    override suspend fun addModificationOption(modificationOption: ModificationOption): Int = dbQuery{
        ModificationOptions.insert{
            it[ModificationOptions.modOptionType] = modificationOption.modOptionType
            it[ModificationOptions.modOptionLabel] = modificationOption.modOptionLabel
        }[ModificationOptions.modOptionId]
    }

    override suspend fun addInductionOption(inductionOption: InductionOption): Int = dbQuery{
        InductionOptions.insert{
            it[InductionOptions.inductionType] = inductionOption.inductionType
            it[InductionOptions.inductionLabel] = inductionOption.inductionLabel
        }[InductionOptions.inductionOptionId]
    }

    override suspend fun addDrivetrainOption(drivetrainOption: DrivetrainOption): Int = dbQuery{
        DrivetrainOptions.insert{
            it[DrivetrainOptions.drivetrainType] = drivetrainOption.drivetrainType
            it[DrivetrainOptions.drivetrainLabel] = drivetrainOption.drivetrainLabel
        }[DrivetrainOptions.drivetrainOptionId]
    }

    override suspend fun addTransmissionOption(transmissionOption: TransmissionOption): Int = dbQuery{
        TransmissionOptions.insert{
            it[TransmissionOptions.transmissionType] = transmissionOption.transmissionType
            it[TransmissionOptions.transmissionLabel] = transmissionOption.transmissionLabel
        }[TransmissionOptions.transmissionOptionId]
    }

    override suspend fun addPowertrainconfigOption(powertrainConfigOption: PowertrainConfigOption): Int = dbQuery{
        PowertrainConfigOptions.insert{
            it[PowertrainConfigOptions.powertrainConfigType] = powertrainConfigOption.powertrainConfigType
            it[PowertrainConfigOptions.powertrainConfigLabel] = powertrainConfigOption.powertrainConfigLabel
        }[PowertrainConfigOptions.powertrainConfigOptionId]
    }

    override suspend fun addEngineOption(engineOption: EngineOption): Int = dbQuery{
        EngineOptions.insert{
            it[EngineOptions.engineType] = engineOption.engineType
            it[EngineOptions.engineLabel] = engineOption.engineLabel
        }[EngineOptions.engineOptionId]
    }

    override suspend fun addVehicleRating(vehicleRating: VehicleRating): Int {
        TODO("Not yet implemented")
    }

    override suspend fun updateVehicleRating(vehicleRating: VehicleRating): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteVehicleRating(vehicleRatingId: Int): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getVehicleRatingByVehicleId(vehicleId: Int): Int {
        TODO("Not yet implemented")
    }

    override suspend fun getStates(parent: Int): List<State> = dbQuery{
        (Countries innerJoin AddressStates)
            .slice(Countries.countryId, AddressStates.stateId, AddressStates.name, AddressStates.code, AddressStates.parent)
            .select(AddressStates.parent eq Countries.countryId)
            .andWhere {AddressStates.parent eq parent}
            .map(::resultToState)
    }

    override suspend fun getAllStates(): List<State>  = dbQuery{
        AddressStates.selectAll().map(::resultToState)
    }

    override suspend fun getStateById(stateId: Int): State = dbQuery{
        AddressStates.select(AddressStates.stateId eq stateId).single().let(::resultToState)
    }

    override suspend fun getCountryById(countryId: Int): Country = dbQuery{
        Countries.select(Countries.countryId eq countryId).single().let(::resultToCountry)
    }

    override suspend fun getCountries(): List<Country> = dbQuery{
        Countries.selectAll().map(::resultToCountry)
    }

    override suspend fun addState(state: State): Boolean = dbQuery{
        AddressStates.insert{
            it[AddressStates.name] = state.name
            it[AddressStates.code] = state.code
            it[AddressStates.parent] = state.parent
        }[AddressStates.stateId] > -1
    }

    override suspend fun addCountry(country: Country): Boolean = dbQuery{
        Countries.insert{
            it[Countries.name] = country.name
            it[Countries.code] = country.code
        }[Countries.countryId] > -1
    }

    override suspend fun deleteCountries(): Boolean = dbQuery{
        Countries.deleteAll() > 0
    }

    override suspend fun deleteStates(): Boolean = dbQuery{
        AddressStates.deleteAll() > 0
    }

    override suspend fun countCountries(): Int = dbQuery{
        Countries.selectAll().count().toInt()
    }

    override suspend fun countStates(): Int = dbQuery{
        AddressStates.selectAll().count().toInt()
    }

    override suspend fun getVehiclePhotos(vehicleId: Int): List<VehiclePhoto> = dbQuery{
        VehiclePhotos.select(VehiclePhotos.vehicleId eq vehicleId).map(::resultToVehiclePhoto)
    }

    override suspend fun getVehiclePhoto(photoId: Int): VehiclePhoto? = dbQuery{
        VehiclePhotos.select(VehiclePhotos.vehiclePhotoId eq photoId).map(::resultToVehiclePhoto).singleOrNull()
    }

    override suspend fun getUserPhoto(userId: Int): UserPhoto? = dbQuery{
        UserPhotos.select(UserPhotos.userId eq userId).map(::resultToUserPhoto).singleOrNull()
    }

    override suspend fun getVehicleMainPhoto(vehicleId: Int): VehiclePhoto? = dbQuery{
        VehiclePhotos.select(VehiclePhotos.vehicleId eq vehicleId)
            .andWhere { VehiclePhotos.primary eq true }
            .map(::resultToVehiclePhoto)
            .singleOrNull()
    }

    override suspend fun getLicensePhoto(licenseId: Int): LicensePhoto? = dbQuery{
        LicensePhotos.select(LicensePhotos.licenseId eq licenseId)
            .map(::resultToLicensePhoto)
            .singleOrNull()
    }

    override suspend fun getInsurancePhoto(insuranceId: Int): InsurancePhoto? = dbQuery{
        InsurancePhotos.select(InsurancePhotos.insuranceId eq insuranceId)
            .map(::resultToInsurancePhoto)
            .singleOrNull()
    }

    override suspend fun updateVehiclePhoto(photo: VehiclePhoto): VehiclePhoto? = dbQuery{
        VehiclePhotos.upsert {
            it[VehiclePhotos.vehiclePhotoId] = photo.vehiclePhotoId
            it[VehiclePhotos.location] = photo.location
            it[VehiclePhotos.vehicleId] = photo.vehicleId
            it[VehiclePhotos.primary] = photo.primary
            it[VehiclePhotos.caption] = photo.caption
            it[VehiclePhotos.name] = photo.name
            it[VehiclePhotos.bucket] = photo.bucket
        }.resultedValues?.singleOrNull()?.let(::resultToVehiclePhoto)
    }

    override suspend fun updateUserPhoto(photo: UserPhoto): UserPhoto? = dbQuery{
        if(UserPhotos.update {
            it[UserPhotos.userPhotoId] = photo.userPhotoId
            it[UserPhotos.location] = photo.location
            it[UserPhotos.userId] = photo.userId
            it[UserPhotos.bucket] = photo.bucket
            it[UserPhotos.name] = photo.name
        } > 0)
            photo
        else
            null
    }

    override suspend fun updateLicensePhoto(photo: LicensePhoto): LicensePhoto? = dbQuery{
        if(LicensePhotos.update {
            it[LicensePhotos.licensePhotoId] = photo.licensePhotoId
            it[LicensePhotos.location] = photo.location
            it[LicensePhotos.licenseId] = photo.licenseId
        } > 0)
            photo
        else
            null
    }

    override suspend fun updateInsurancePhoto(photo: InsurancePhoto): InsurancePhoto? = dbQuery{
        if(InsurancePhotos.update{
            it[InsurancePhotos.insurancePhotoId] = photo.insurancePhotoId
            it[InsurancePhotos.location] = photo.location
            it[InsurancePhotos.insuranceId] = photo.insuranceId
        } > 0)
            photo
        else
            null
    }

    override suspend fun addUserPhoto(photo: UserPhoto): UserPhoto? = dbQuery{
        UserPhotos.insert{
            it[UserPhotos.location] = photo.location
            it[UserPhotos.userId] = photo.userId
            it[UserPhotos.bucket] = photo.bucket
            it[UserPhotos.name] = photo.name
        }.resultedValues?.singleOrNull()?.let(::resultToUserPhoto)
    }

    override suspend fun addVehiclePhoto(photo: VehiclePhoto): VehiclePhoto? = dbQuery{
        VehiclePhotos.insert{
            it[VehiclePhotos.location] = photo.location
            it[VehiclePhotos.vehicleId] = photo.vehicleId
            it[VehiclePhotos.primary] = photo.primary
            it[VehiclePhotos.caption] = photo.caption
            it[VehiclePhotos.name] = photo.name
            it[VehiclePhotos.bucket] = photo.bucket
        }.resultedValues?.singleOrNull()?.let(::resultToVehiclePhoto)
    }

    override suspend fun addLicensePhoto(photo: LicensePhoto): LicensePhoto? = dbQuery{
        LicensePhotos.insert{
            it[LicensePhotos.location] = photo.location
            it[LicensePhotos.licenseId] = photo.licenseId
        }.resultedValues?.singleOrNull()?.let(::resultToLicensePhoto)
    }

    override suspend fun addInsurancePhoto(photo: InsurancePhoto): InsurancePhoto? = dbQuery{
        InsurancePhotos.insert{
            it[InsurancePhotos.location] = photo.location
            it[InsurancePhotos.insuranceId] = photo.insuranceId
        }.resultedValues?.singleOrNull()?.let(::resultToInsurancePhoto)
    }

    override suspend fun deleteUserPhoto(photoId: Int): Boolean = dbQuery{
        UserPhotos.deleteWhere { UserPhotos.userPhotoId eq photoId } > 0
    }

    override suspend fun deleteVehiclePhoto(photoId: Int): Boolean = dbQuery{
        VehiclePhotos.deleteWhere { VehiclePhotos.vehiclePhotoId eq photoId } > 0
    }

    override suspend fun addLicense(license: License): Int = dbQuery{
        val insert = Licenses.insert{
            it[Licenses.license] = license.license
            it[Licenses.stateId] = license.stateId
            it[Licenses.countryId] = license.countryId
            it[Licenses.issueDate] = license.issueDate
            it[Licenses.expirationDate] = license.expirationDate
        }

        insert.resultedValues?.singleOrNull()?.let(::resultToLicense)!!.licenseId
    }

    override suspend fun deleteLicense(id: Int): Boolean = dbQuery{
        Licenses.deleteWhere { Licenses.licenseId eq id } > 0
    }

    override suspend fun getLicense(id: Int): License? = dbQuery{
        Licenses.select(Licenses.licenseId eq id).map(::resultToLicense).singleOrNull()
    }


    override suspend fun createReservation(reservation: Reservation): Reservation? = dbQuery{
        val insert = Reservations.insert{
            it[Reservations.vehicleId] = reservation.vehicleId
            it[Reservations.userId] = reservation.userId
            it[Reservations.dateCreated] = reservation.dateCreated
            it[Reservations.dateReservedStart] = reservation.dateReservedStart
            it[Reservations.dateReservedEnd] = reservation.dateReservedEnd
        }

        insert.resultedValues?.singleOrNull()?.let(::resultToReservation)
    }

    override suspend fun deleteReservation(id: Int): Boolean = dbQuery{
        Reservations.deleteWhere { Reservations.reservationId eq id } > 0
    }

    override suspend fun getReservation(id: Int): Reservation? = dbQuery{
        Reservations.select(Reservations.reservationId eq id).map(::resultToReservation).singleOrNull()
    }

    override suspend fun getUserReservations(userId: Int): List<Reservation>? = dbQuery{
        Reservations.select(Reservations.userId eq userId).map(::resultToReservation)
    }

    override suspend fun getVehicleReservations(vehicleId: Int): List<Reservation>? = dbQuery{
        Reservations.select(Reservations.vehicleId eq vehicleId).map(::resultToReservation)
    }

    override suspend fun getUserTransactions(userId: Int): List<Transaction>? = dbQuery{
        AppTransactions.select(AppTransactions.userId eq userId).map(::resultToTransaction)
    }

    override suspend fun getTransaction(id: Int): Transaction? = dbQuery{
        AppTransactions.select(AppTransactions.transactionId eq id).map(::resultToTransaction).singleOrNull()
    }

    override suspend fun addTransaction(transaction: Transaction): Transaction? = dbQuery{
        val insert = AppTransactions.insert {
            it[AppTransactions.userId] = transaction.userId
            it[AppTransactions.extTransactionId] = transaction.extTransactionId
            it[AppTransactions.reservationId] = transaction.reservationId
            it[AppTransactions.last4] = transaction.last4
            it[AppTransactions.cardType] = transaction.cardType
            it[AppTransactions.nameOnCard] = transaction.nameOnCard
            it[AppTransactions.amount] = transaction.amount
            it[AppTransactions.date] = transaction.date
            it[AppTransactions.refunded] = transaction.refunded
            it[AppTransactions.discounted] = transaction.discounted
        }

        insert.resultedValues?.singleOrNull()?.let(::resultToTransaction)
    }

    override suspend fun updateTransaction(transaction: Transaction): Transaction? = dbQuery{
        if(AppTransactions.update({AppTransactions.transactionId eq transaction.transactionId}){
                it[AppTransactions.transactionId] = transaction.transactionId
                it[AppTransactions.userId] = transaction.userId
                it[AppTransactions.extTransactionId] = transaction.extTransactionId
                it[AppTransactions.reservationId] = transaction.reservationId
                it[AppTransactions.last4] = transaction.last4
                it[AppTransactions.cardType] = transaction.cardType
                it[AppTransactions.amount] = transaction.amount
                it[AppTransactions.nameOnCard] = transaction.nameOnCard
                it[AppTransactions.date] = transaction.date
                it[AppTransactions.refunded] = transaction.refunded
                it[AppTransactions.discounted] = transaction.discounted
            } > 0)
                transaction
        else
            null
    }

    override suspend fun countVehicleMakes(): Int = dbQuery{
        VehicleMakes.selectAll().count().toInt()
    }

    override suspend fun countVehicleModels(): Int = dbQuery{
        VehicleModels.selectAll().count().toInt()
    }

    override suspend fun addVehicleModel(model: VehicleModel): Int = dbQuery{
        VehicleModels.insert{
            it[VehicleModels.model] = model.model
            it[VehicleModels.makeId] = model.makeId
        }[VehicleModels.vehicleModelId]
    }

    override suspend fun addVehicleMake(make: VehicleMake): Int = dbQuery{
        VehicleMakes.insert{
            it[VehicleMakes.make] = make.make
        }[VehicleMakes.vehicleMakeId]
    }

    override suspend fun getVehicleMakes(): List<VehicleMake> = dbQuery{
        VehicleMakes.selectAll().map(::resultToVehicleMake)
    }

    override suspend fun getVehicleModels(makeId: Int): List<VehicleModel> = dbQuery{
        VehicleModels.select(VehicleModels.makeId eq makeId).map(::resultToVehicleModel)
    }

    override suspend fun getAllVehicleModels(): List<VehicleModel> = dbQuery{
        VehicleModels.selectAll().map(::resultToVehicleModel)
    }

    override suspend fun deleteVehicleModels(): Boolean = dbQuery{
        VehicleModels.deleteAll() > 0
    }

    override suspend fun createTempUser(tempUser: TempUser): TempUser? = dbQuery{
        val insert = TempUsers.insert {
            it[TempUsers.email] = tempUser.email
            it[TempUsers.firebaseToken] = tempUser.firebaseToken
            it[TempUsers.pass] = tempUser.password
        }

        insert.resultedValues?.singleOrNull()?.let(::resultToTempUser)
    }

    override suspend fun getTempUser(email: String): TempUser? = dbQuery{
        TempUsers.select(TempUsers.email eq email).map(::resultToTempUser).singleOrNull()
    }

    override suspend fun doesTempUserExist(email: String): Boolean = dbQuery{
        TempUsers.select(TempUsers.email eq email).count() > 0
    }

    override suspend fun updateAuthToken(authToken: AuthToken): AuthToken? = dbQuery{
         val update = AppAuthTokens.update{
             it[AppAuthTokens.authTokenId] = authToken.id
             it[AppAuthTokens.token] = authToken.token
             it[AppAuthTokens.userId] = authToken.userId
         }

        if(update > 0)
            authToken
        else
            null
    }

    override suspend fun addAuthToken(authToken: AuthToken): AuthToken? = dbQuery{
        val inserted = AppAuthTokens.insert{
            it[AppAuthTokens.token] = authToken.token
            it[AppAuthTokens.userId] = authToken.userId
        }

        inserted.resultedValues?.singleOrNull()?.let(::resultToAuthToken)
    }

    override suspend fun getAuthTokenByUserId(userId: Int): AuthToken? = dbQuery{
        AppAuthTokens.select(AppAuthTokens.userId eq userId).map(::resultToAuthToken).singleOrNull()
    }

    override suspend fun updateFirebaseToken(firebaseToken: FirebaseMessagingToken): Boolean = dbQuery{
        FirebaseMessagingTokens.update {
            it[FirebaseMessagingTokens.firebaseMessageId] = firebaseToken.id!!
            it[FirebaseMessagingTokens.token] = firebaseToken.token
            it[FirebaseMessagingTokens.userId] = firebaseToken.userId
            it[FirebaseMessagingTokens.expirationDate] = firebaseToken.expirationDate
        } > 0
    }

    override suspend fun addFirebaseToken(firebaseToken: FirebaseMessagingToken): Int = dbQuery{
        val insert = FirebaseMessagingTokens.insert{
            it[FirebaseMessagingTokens.token] = firebaseToken.token
            it[FirebaseMessagingTokens.userId] = firebaseToken.userId
            it[FirebaseMessagingTokens.expirationDate] = firebaseToken.expirationDate
        }

        insert.resultedValues?.single()?.let(::resultToFirebaseMessagingToken)!!.id!!
    }

    override suspend fun getFirebaseTokenByUserId(userId: Int): String = dbQuery{
        FirebaseMessagingTokens.select(FirebaseMessagingTokens.userId eq userId).map(::resultToFirebaseMessagingToken).single().token
    }

    private fun resultToFirebaseMessagingToken(row: ResultRow) = FirebaseMessagingToken(
        id = row[FirebaseMessagingTokens.firebaseMessageId],
        token = row[FirebaseMessagingTokens.token],
        userId = row[FirebaseMessagingTokens.userId],
        expirationDate = row[FirebaseMessagingTokens.expirationDate]
    )

    private fun resultToAuthToken(row: ResultRow) = AuthToken(
        id = row[AppAuthTokens.authTokenId],
        token = row[AppAuthTokens.token],
        userId = row[AppAuthTokens.userId]
    )

    private fun resultToTempUser(row: ResultRow) = TempUser(
        id = row[TempUsers.tempId],
        email = row[TempUsers.email],
        firebaseToken = row[TempUsers.firebaseToken],
        password = row[TempUsers.pass]
    )
    private fun resultToVehicleMake(row: ResultRow) = VehicleMake(
        vehicleMakeId = row[VehicleMakes.vehicleMakeId],
        make = row[VehicleMakes.make]
    )

    private fun resultToVehicleModel(row: ResultRow) = VehicleModel(
        vehicleModelId = row[VehicleModels.vehicleModelId],
        model = row[VehicleModels.model],
        makeId = row[VehicleModels.makeId]
    )

    private fun resultToUser(row: ResultRow) = AppUser(
        id = row[Customers.id],
        firstname = row[Customers.firstname],
        lastname = row[Customers.lastname],
        username = row[Customers.username],
        email = row[Customers.email],
        phone = row[Customers.phone],
        customerId = row[Customers.customerId]
    )

    private fun resultToVehicle(row: ResultRow) = CustomerVehicle(
        vehicleId = row[Vehicles.vehicleId],
        makeId = row[Vehicles.makeId],
        modelId = row[Vehicles.modelId],
        price = row[Vehicles.price],
        year = row[Vehicles.year],
        plate = row[Vehicles.plate],
        color = row[Vehicles.color],
        description = row[Vehicles.description],
        vin = row[Vehicles.vin],
        userId = row[Vehicles.userId]
    )

    private fun resultToVehicleOptions(row: ResultRow) = VehicleOptions(
        vehicleOptionsId = row[VehicleOptionsTable.vehicleOptionsId],
        drivetrainOptionId = row[VehicleOptionsTable.drivetrainOptionId],
        engineOptionId = row[VehicleOptionsTable.engineOptionId],
        powertrainConfigId = row[VehicleOptionsTable.powertrainConfigOptionId],
        inductionOptionId = row[VehicleOptionsTable.inductionOptionId],
        transmissionOptionId = row[VehicleOptionsTable.transmissionOptionId],
        vehicleOptionsVehicleId = row[VehicleOptionsTable.vehicleOptionsVehicleId]
    )

    private fun resultToCountry(row: ResultRow) = Country(
        id = row[Countries.countryId],
        code = row[Countries.code],
        name = row[Countries.name]
    )

    private fun resultToState(row: ResultRow) = State(
        id = row[AddressStates.stateId],
        code = row[AddressStates.code],
        name = row[AddressStates.name],
        parent = row[AddressStates.parent]
    )

    private fun resultToUserAddress(row: ResultRow) = UserAddress(
        addressId = row[AppUserAddresses.userAddressId],
        street = row[AppUserAddresses.street],
        address2 = row[AppUserAddresses.address2],
        city = row[AppUserAddresses.city],
        stateId = row[AppUserAddresses.state],
        countryId = row[AppUserAddresses.country],
        zipcode = row[AppUserAddresses.zipcode],
        userId = row[AppUserAddresses.userId],
        externalId = row[AppUserAddresses.externalId]
    )

    private fun resultToVehicleAddress(row: ResultRow) = AppVehicleAddress(
        vehicleAddressId = row[VehicleAddresses.vehicleAddressId],
        street = row[VehicleAddresses.street],
        city = row[VehicleAddresses.city],
        stateId = row[VehicleAddresses.state],
        countryId = row[VehicleAddresses.country],
        zipcode = row[VehicleAddresses.zipcode],
        vehicleId = row[VehicleAddresses.vehicleId],
        lat = row[VehicleAddresses.lat],
        long = row[VehicleAddresses.long]
    )

    private fun resultToLicense(row: ResultRow) = License(
        licenseId = row[Licenses.licenseId],
        license = row[Licenses.license],
        issueDate = row[Licenses.issueDate],
        expirationDate = row[Licenses.expirationDate],
        stateId = row[Licenses.stateId],
        countryId = row[Licenses.countryId],
        approved = row[Licenses.approved]
    )

    private fun resultToVehiclePhoto(row: ResultRow) = VehiclePhoto(
        vehiclePhotoId = row[VehiclePhotos.vehiclePhotoId],
        location = row[VehiclePhotos.location],
        vehicleId = row[VehiclePhotos.vehicleId],
        primary = row[VehiclePhotos.primary],
        caption = row[VehiclePhotos.caption],
        name = row[VehiclePhotos.name],
        bucket = row[VehiclePhotos.bucket]
    )

    private fun resultToUserPhoto(row: ResultRow) = UserPhoto(
        userPhotoId = row[UserPhotos.userPhotoId],
        location = row[UserPhotos.location],
        userId = row[UserPhotos.userId],
        bucket = row[UserPhotos.bucket],
        name = row[UserPhotos.name]
    )

    private fun resultToLicensePhoto(row: ResultRow) = LicensePhoto(
        licensePhotoId = row[LicensePhotos.licensePhotoId],
        location = row[LicensePhotos.location],
        licenseId = row[LicensePhotos.licenseId]
    )

    private fun resultToInsurancePhoto(row: ResultRow) = InsurancePhoto(
        insurancePhotoId = row[InsurancePhotos.insurancePhotoId],
        location = row[InsurancePhotos.location],
        insuranceId = row[InsurancePhotos.insuranceId]
    )

    private fun resultToReservation(row: ResultRow) = Reservation(
        reservationId = row[Reservations.reservationId],
        vehicleId = row[Reservations.vehicleId],
        userId = row[Reservations.userId],
        dateCreated = row[Reservations.dateCreated],
        dateReservedStart = row[Reservations.dateReservedStart],
        dateReservedEnd = row[Reservations.dateReservedEnd]
    )

    private fun resultToTransaction(row: ResultRow) = Transaction(
        transactionId = row[AppTransactions.transactionId],
        userId = row[AppTransactions.userId],
        extTransactionId = row[AppTransactions.extTransactionId],
        reservationId = row[AppTransactions.reservationId],
        last4 = row[AppTransactions.last4],
        cardType = row[AppTransactions.cardType],
        date = row[AppTransactions.date],
        amount = row[AppTransactions.amount],
        nameOnCard = row[AppTransactions.nameOnCard],
        refunded = row[AppTransactions.refunded],
        discounted = row[AppTransactions.discounted]
    )

    private fun resultToDrivetrainOption(row: ResultRow) = DrivetrainOption(
        drivetrainOptionId = row[DrivetrainOptions.drivetrainOptionId],
        drivetrainType = row[DrivetrainOptions.drivetrainType],
        drivetrainLabel = row[DrivetrainOptions.drivetrainLabel]
    )

    private fun resultToEngineOption(row: ResultRow) = EngineOption(
        engineOptionId = row[EngineOptions.engineOptionId],
        engineType = row[EngineOptions.engineType],
        engineLabel = row[EngineOptions.engineLabel]
    )

    private fun resultToInductionOption(row: ResultRow) = InductionOption(
        inductionOptionId = row[InductionOptions.inductionOptionId],
        inductionType = row[InductionOptions.inductionType],
        inductionLabel = row[InductionOptions.inductionLabel]
    )

    private fun resultToModification(row: ResultRow) = Modification(
        modId = row[Modifications.modId],
        modTypeId = row[Modifications.modTypeId],
        modVehicleId = row[Modifications.modVehicleId],
        input = row[Modifications.input]
    )

    private fun resultToModificationOption(row: ResultRow) = ModificationOption(
        modOptionId = row[ModificationOptions.modOptionId],
        modOptionType = row[ModificationOptions.modOptionType],
        modOptionLabel = row[ModificationOptions.modOptionLabel]
    )

    private fun resultToPowertrainConfigOption(row: ResultRow) = PowertrainConfigOption(
        powertrainConfigOptionId = row[PowertrainConfigOptions.powertrainConfigOptionId],
        powertrainConfigType = row[PowertrainConfigOptions.powertrainConfigType],
        powertrainConfigLabel = row[PowertrainConfigOptions.powertrainConfigLabel]
    )

    private fun resultToTransmissionOption(row: ResultRow) = TransmissionOption(
        transmissionOptionId = row[TransmissionOptions.transmissionOptionId],
        transmissionType = row[TransmissionOptions.transmissionType],
        transmissionLabel = row[TransmissionOptions.transmissionLabel]
    )

    private fun resultToVehicleRating(row: ResultRow) = VehicleRating(
        vehicleRatingId = row[VehicleRatings.vehicleRatingId],
        rating = row[VehicleRatings.rating],
        vehicleId = row[VehicleRatings.vehicleId]
    )

    private fun resultToPassword(row: ResultRow) = Password(
        id = row[Passes.passwordId],
        userId = row[Passes.userId],
        value = row[Passes.value]
    )
}