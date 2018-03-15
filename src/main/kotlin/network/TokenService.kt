package network

import config.Config
import domain.TokenResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST

interface TokenService {

    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("token")
    fun getToken(
            @Field("grant_type") grantType: String = "password",
            @Field("username") username: String = Config.USERNAME,
            @Field("password") password: String = Config.PASSWORD
    ):
            Call<TokenResponse>

}