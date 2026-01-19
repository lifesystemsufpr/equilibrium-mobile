package com.ufpr.equilibrium.data.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ufpr.equilibrium.data.remote.PessoasService
import com.ufpr.equilibrium.data.BuildConfig
import android.app.Application
import com.ufpr.equilibrium.data.network.AuthInterceptor
import com.ufpr.equilibrium.data.network.UnauthorizedInterceptor
import com.ufpr.equilibrium.domain.auth.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenProvider: TokenProvider): AuthInterceptor =
        AuthInterceptor(tokenProvider)

    @Provides
    @Singleton
    fun provideUnauthorizedInterceptor(application: Application): UnauthorizedInterceptor =
        UnauthorizedInterceptor(application)


    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        unauthorizedInterceptor: UnauthorizedInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                // Enable logging in debug or when explicitly enabled in release
                if (BuildConfig.DEBUG || BuildConfig.ENABLE_LOGGING) {
                    val logging = HttpLoggingInterceptor().apply { 
                        level = HttpLoggingInterceptor.Level.BODY 
                    }
                    addInterceptor(logging)
                }
            }
            .addInterceptor(authInterceptor)
            .addInterceptor(unauthorizedInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun providePessoasService(retrofit: Retrofit): PessoasService =
        retrofit.create(PessoasService::class.java)
}


