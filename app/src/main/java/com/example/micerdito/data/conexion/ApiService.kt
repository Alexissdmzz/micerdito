package com.example.micerdito.data.conexion

import com.example.micerdito.data.model.autenticacion.*
import com.example.micerdito.data.model.home.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * INTERFAZ - ApiService
 * Define los endpoints del backend.
 * Utiliza Corrutinas (suspend) para garantizar la asincronía y no bloquear el hilo principal.
 * Las respuestas se envuelven en Response<T> para delegar el manejo de códigos HTTP (200, 400, 500) al Repositorio.
 */
interface ApiService {

    // ==========================================
    // CAPA DE AUTENTICACIÓN Y SEGURIDAD
    // ==========================================

    @FormUrlEncoded
    @POST("autenticacion/login.php")
    suspend fun loginUser(
        @Field("correo") email: String,
        @Field("pwd") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("autenticacion/registro.php")
    suspend fun registerUser(
        @Field("nombre_usuario") username: String,
        @Field("correo") email: String,
        @Field("pwd") pwd: String,
        @Field("repeat_pwd") repeatpwd: String,
        @Field("id_pregunta") id: Int,
        @Field("respuesta_seguridad") res: String
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("autenticacion/obtener_pregunta.php")
    suspend fun getPregunta(
        @Field("correo") email: String
    ): Response<ForgotPasswordResponse>

    @FormUrlEncoded
    @POST("autenticacion/cambiar_pwd.php")
    suspend fun cambiarPwd(
        @Field("correo") email: String,
        @Field("respuesta_seguridad") respuesta: String,
        @Field("nueva_pwd") nuevaPwd: String
    ): Response<ForgotPasswordResponse>

    // ==========================================
    // CAPA PRINCIPAL (HOME)
    // ==========================================

    @GET("home/obtener_datos.php")
    suspend fun homeUser(
        @Query("id_usuario") id: String
    ): Response<HomeResponse>

    @GET("home/grafico_gastos.php")
    suspend fun obtenerGastosGrafico(
        @Query("id_usuario") id: String
    ): Response<GraficoResponse>

    @GET("home/obtener_movimientos.php")
    suspend fun homeMoves(
        @Query("id_usuario") id: String
    ): Response<MovimientosResponse>

    @FormUrlEncoded
    @POST("home/guardar_limite.php")
    suspend fun homeLimit(
        @Field("id_usuario") id: String,
        @Field("limite") limite: Double
    ): Response<LimiteResponse>

    // ==========================================
    // CAPA DE AJUSTES (PERFIL)
    // ==========================================

    @FormUrlEncoded
    @POST("ajustes/borrar_usuario.php")
    suspend fun deleteUser(
        @Field("id_usuario") id: String
    ): Response<AjustesResponse>

    @FormUrlEncoded
    @POST("ajustes/editar_nombre_usuario.php")
    suspend fun editUser(
        @Field("id_usuario") id: String,
        @Field("nombre_usuario") username: String
    ): Response<AjustesResponse>

    // ==========================================
    // CAPA DE GESTIÓN DE GASTOS
    // ==========================================
    @GET("gastos/obtener_categorias.php")
    suspend fun getCategorias(): Response<CategoriaResponse>

    // Uso de @Multipart para permitir la transmisión de archivos binarios (fotos) junto con datos de texto
    @Multipart
    @POST("gastos/insertar_gastos.php")
    suspend fun insertGasto(
        @Part("id_usuario") id: RequestBody,
        @Part("id_categoria") idCategoria: RequestBody,
        @Part("titulo") titulo: RequestBody,
        @Part("importe") importe: RequestBody,
        @Part("fecha_gasto") fechaGasto: RequestBody,
        @Part("descripcion") descripcion: RequestBody?,
        @Part foto: MultipartBody.Part?
    ): Response<GastoResponse>

    @Multipart
    @POST("calendario/editar_gasto.php")
    suspend fun editGasto(
        @Part("id_usuario") idUsuario: RequestBody,
        @Part("id_gasto") idGasto: RequestBody,
        @Part("titulo") titulo: RequestBody,
        @Part("importe") importe: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("foto_ticket") fotoActual: RequestBody,
        @Part("fecha_gasto") fecha: RequestBody,
        @Part foto: MultipartBody.Part?
    ): Response<GastoResponse>

    @FormUrlEncoded
    @POST("calendario/eliminar_gasto.php")
    suspend fun deleteGasto(
        @Field("id_usuario") idUsuario: String,
        @Field("id_gasto") idGasto: String
    ): Response<GastoResponse>

    // ==========================================
    // CAPA DE CALENDARIO
    // ==========================================

    @GET("calendario/obtener_datos_calendario.php")
    suspend fun getDataCalendario(
        @Query("id_usuario") id: String,
        @Query("mes") mes: Int,
        @Query("anio") anio: Int
    ): Response<CalendarioResponse>

    @GET("calendario/obtener_gastos_dia.php")
    suspend fun getGastosDia(
        @Query("id_usuario") id: String,
        @Query("anio") anio: Int,
        @Query("mes") mes: Int,
        @Query("dia") dia: Int
    ): Response<GastoResponse>
}