package com.ducktapedapps.updoot.di

import com.ducktapedapps.updoot.BuildConfig
import com.ducktapedapps.updoot.data.local.model.Comment.CommentData
import com.ducktapedapps.updoot.data.local.model.Comment.MoreCommentData
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.data.local.moshiAdapters.ImageJsonAdapterFactory
import com.ducktapedapps.updoot.data.local.moshiAdapters.UpdootAdapterFactory
import com.ducktapedapps.updoot.data.local.moshiAdapters.VideoJsonAdapterFactory
import com.ducktapedapps.updoot.utils.Constants
import com.ducktapedapps.updoot.utils.accountManagement.TokenInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.Reusable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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

            //For removing default legacy json character support
            val url = chain.request().url.newBuilder().addQueryParameter("raw_json", "1").build()

            val request = chain.request().newBuilder().url(url).addHeader("User-Agent", userAgent).build()
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
    fun provideMoshi(): Moshi = Moshi
            .Builder()
            .add(VideoJsonAdapterFactory())
            .add(ImageJsonAdapterFactory())
            .add(
                    UpdootAdapterFactory.of("kind", "data")
                            .withSubType("t1", CommentData::class.java)
                            .withSubType("t3", LinkData::class.java)
                            .withSubType("t5", Subreddit::class.java)
                            .withSubType("more", MoreCommentData::class.java)
            )
            .build()

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
            .baseUrl(Constants.API_BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create(moshi).asLenient())
            .client(client)
            .build()

}