package com.aserdipanda.synapse.core.network

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkModule {
    
    private var okHttpClient: OkHttpClient? = null
    
    fun provideOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
        return okHttpClient!!
    }
}
