package com.targaryen.cafeteria.coredatabase.address

import android.content.Context
import org.koin.core.annotation.Single

data class DeliveryAddress(
    val cep: String = "",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val number: String = "",
    val referencePoint: String = "",
    val recipientName: String = "",
)

@Single
class AddressProvider(
    private val context: Context,
) {
    private val prefs = context.getSharedPreferences("delivery_address_prefs", Context.MODE_PRIVATE)

    fun saveAddress(address: DeliveryAddress) {
        prefs
            .edit()
            .putString("cep", address.cep)
            .putString("street", address.street)
            .putString("city", address.city)
            .putString("state", address.state)
            .putString("number", address.number)
            .putString("referencePoint", address.referencePoint)
            .putString("recipientName", address.recipientName)
            .apply()
    }

    fun getAddress(): DeliveryAddress =
        DeliveryAddress(
            cep = prefs.getString("cep", "") ?: "",
            street = prefs.getString("street", "") ?: "",
            city = prefs.getString("city", "") ?: "",
            state = prefs.getString("state", "") ?: "",
            number = prefs.getString("number", "") ?: "",
            referencePoint = prefs.getString("referencePoint", "") ?: "",
            recipientName = prefs.getString("recipientName", "") ?: "",
        )

    fun clearAddress() {
        prefs.edit().clear().apply()
    }
}
