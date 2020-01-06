package com.ducktapedapps.updoot.di

import com.ducktapedapps.updoot.BuildConfig
import com.ducktapedapps.updoot.model.CommentData
import com.ducktapedapps.updoot.model.Thing
import com.ducktapedapps.updoot.utils.CommentDeserializer
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.ThingDeserializer
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module
class NetworkModule {
    @Singleton
    @Provides
    fun provideOkHttpClient(tokenInterceptor: TokenInterceptor): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
        okHttpClient.addNetworkInterceptor(tokenInterceptor)
        okHttpClient.addNetworkInterceptor(Interceptor { chain: Interceptor.Chain ->
            //as per reddit api guidelines to include proper user agent
            val userAgent = "android:com.ducktapedapps.updoot:" + BuildConfig.VERSION_NAME + " (by /u/nothoneypot)"
            val request = chain.request().newBuilder().addHeader("User-Agent", userAgent).build()
            chain.proceed(request)
        })
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BASIC
            okHttpClient.addInterceptor(httpLoggingInterceptor)
        }
        return okHttpClient.build()
    }


    @Reusable
    @Provides
    fun provideGson(): Gson {
        val gsonBuilder = GsonBuilder()
                .registerTypeAdapter(Thing::class.java, ThingDeserializer())
                .registerTypeAdapter(CommentData::class.java, CommentDeserializer())
        return gsonBuilder.create()
    }


    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.API_BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build()
    }
}