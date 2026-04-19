package com.example.micerdito.data.conexion

import com.example.micerdito.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * PATRÓN SINGLETON - RetrofitClient
 * Cliente HTTP centralizado para la capa de datos.
 * Garantiza que toda la aplicación comparta una única instancia de conexión y socket HTTP,
 * optimizando los recursos de red y la memoria del dispositivo.
 */
object RetrofitClient {

    // Configuración de serialización. Se habilita 'Lenient' como mecanismo de tolerancia a fallos
    // ante posibles cabeceras malformadas o trazas de debug no deseadas emitidas por el backend PHP.
    private val gson = GsonBuilder()
        .setLenient()
        .create()

    // Interceptor de telemetría. Se inyecta la configuración desde el BuildConfig
    // para garantizar que los datos sensibles (Body) no se filtren en los logs de la versión Release (Producción).
    private val interceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.LOG_HTTP_BODY) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.BASIC
        }
    }

    // Motor de red subyacente
    private val client = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    // Inicialización diferida (Lazy initialization). El objeto Retrofit solo se construye
    // y reserva memoria en el momento exacto en que se realiza la primera petición de red.
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    // Exposición pública del contrato de red (ApiService) para ser inyectado
    // en los Repositorios siguiendo el principio de Inversión de Dependencias en MVVM.
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}