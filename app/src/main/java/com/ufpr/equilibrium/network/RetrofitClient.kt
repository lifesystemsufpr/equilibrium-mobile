package com.ufpr.equilibrium.network

import com.google.gson.GsonBuilder
import com.ufpr.equilibrium.feature_questionnaire.api.QuestionnaireAPI
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object  RetrofitClient {
    private val BASE_URL = "https://devenv.tecnoaging.com.br/backend/"
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(logging)
        .addInterceptor(UnauthorizedInterceptor())
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    // Gson mais leniente para debug
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    val instancePessoasAPI: PessoasAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(PessoasAPI::class.java)
    }

    val instanceQuestionnaireAPI: QuestionnaireAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(QuestionnaireAPI::class.java)
    }
}