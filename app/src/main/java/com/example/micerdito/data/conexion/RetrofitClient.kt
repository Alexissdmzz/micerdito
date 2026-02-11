package com.example.micerdito.data.conexion

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que contiene la configuración de Retrofit.
 * Permite establecer la conexión con el servidor y convertir los datos
 */

object RetrofitClient {

    // URL base del servidor local (Usamos 10.0.2.2 que es el "localhost" de Android)
    private const val BASE_URL = "http://10.0.2.2/micerdito_api/"

    // Configuración de GSON, @setLenient permite procesar JSON aunque no sean perfectos o tengan errores
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Permite ver todo el tráfico de red en el Logcat
    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP que gestiona la conexión técnica
    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    // Inicializamos el retrofit
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Usamos la constante de arriba, no la "X"
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Lo instanciamos para poder usarlo en todo el proyecto siguiendo el patrón MVVM
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}