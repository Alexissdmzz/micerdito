package com.example.micerdito.data.conexion

import com.example.micerdito.data.model.autenticacion.ForgotPasswordResponse
import com.example.micerdito.data.model.autenticacion.LoginResponse
import com.example.micerdito.data.model.autenticacion.RegisterResponse
import com.example.micerdito.data.model.home.AjustesResponse
import com.example.micerdito.data.model.home.Categoria
import com.example.micerdito.data.model.home.CategoriaResponse
import com.example.micerdito.data.model.home.GastosResponse
import com.example.micerdito.data.model.home.HomeResponse
import com.example.micerdito.data.model.home.LimiteResponse
import com.example.micerdito.data.model.home.MovimientosResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Interfaz que define las rutas de la API mediante Retrofit, es decir,
 * gestiona la comunicación entre la APP y el servidor PHP
 */

interface ApiService {

    // Inicio de sesión del usuario
    @FormUrlEncoded
    @POST("autenticacion/login.php")
    suspend fun loginUser(
        @Field("correo") email: String,
        @Field("pwd") password: String
    ): Response<LoginResponse>

    // Registrar usuario
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

    // Obtenemos la pregunta de seguridad del usuario
    @FormUrlEncoded
    @POST("autenticacion/obtener_pregunta.php")
    suspend fun getPregunta(
        @Field("correo") email: String
    ): Response<ForgotPasswordResponse>

    // Verificamos y cambiamos la pwd
    @FormUrlEncoded
    @POST("autenticacion/cambiar_pwd.php")
    suspend fun cambiarPwd(
        @Field("correo") email: String,
        @Field("respuesta_seguridad") respuesta: String,
        @Field("nueva_pwd") nuevaPwd: String
    ): Response<ForgotPasswordResponse>

    // Obtiene los datos principales para la pantalla home
    @FormUrlEncoded
    @POST("home/obtener_datos.php")
    suspend fun homeUser(
        @Field("id_usuario") id: String
    ): Response<HomeResponse>

    @FormUrlEncoded
    @POST("home/obtener_movimientos.php")
    suspend fun homeMoves(
        @Field("id_usuario") id: String
    ): Response<MovimientosResponse>

    @FormUrlEncoded
    @POST("home/guardar_limite.php")
    suspend fun homeLimit(
        @Field("id_usuario") id: String,
        @Field("limite") limite: Double
    ): Response<LimiteResponse>

    // Borra el usuario de la BBDD
    @FormUrlEncoded
    @POST("ajustes/borrar_usuario.php")
    suspend fun deleteUser(
        @Field("id_usuario") id: String
    ): Response<AjustesResponse>

    // Cambia el nombre de usuario de la BBDD
    @FormUrlEncoded
    @POST("ajustes/editar_nombre_usuario.php")
    suspend fun editUser(
        @Field("id_usuario") id: String,
        @Field("nombre_usuario") username: String
    ): Response<AjustesResponse>

    // Obtenemos las categorias guardadas en la BBDD
    @GET("gastos/obtener_categorias.php")
    suspend fun getCategorias(): Response<CategoriaResponse>

    // Insertamos el gasto
    @FormUrlEncoded
    @POST("gastos/insertar_gastos.php")
    suspend fun insertGasto(
        @Field("id_usuario") idUsuario : String,
        @Field("id_categoria") idCategoria : String,
        @Field("titulo") titulo : String,
        @Field("importe") importe : Double,
        @Field("fecha_gasto") fechaGasto: String,
        @Field("descripcion") descripcion: String?
    ): Response<GastosResponse>
}