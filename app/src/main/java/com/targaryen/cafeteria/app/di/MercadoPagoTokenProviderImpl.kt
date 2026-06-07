package com.targaryen.cafeteria.app.di

import com.targaryen.cafeteria.app.BuildConfig
import com.targaryen.cafeteria.core_network.remote.MercadoPagoTokenProvider
import org.koin.core.annotation.Single

@Single
class MercadoPagoTokenProviderImpl : MercadoPagoTokenProvider {
    override fun getAccessToken(): String = BuildConfig.MERCADO_PAGO_ACCESS_TOKEN

    override fun getPublicKey(): String = BuildConfig.MERCADO_PAGO_PUBLIC_KEY
}
