package network

import file.FileUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient


abstract class IbbRepository {

    val fileUtil by lazy { FileUtil() }
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build()
    }

    val tokenRepository by lazy { TokenRepository() }

    protected fun getRetrofit(url: String) =
            Retrofit.Builder()
                    .baseUrl(url)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

}