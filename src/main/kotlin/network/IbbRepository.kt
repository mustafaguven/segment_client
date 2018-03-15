package network

import file.FileUtil
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


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

    protected fun <T> checkError(response: Response<T>) {
        if (response.errorBody() != null) {
            println(response.errorBody().string())
            System.exit(-1)
        }
    }

}