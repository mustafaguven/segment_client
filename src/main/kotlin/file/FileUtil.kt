package file

import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import domain.TokenResponse
import java.io.FileReader
import java.io.FileWriter

class FileUtil {

    companion object {
        const val TOKEN_FILE_NAME = "token.json"
    }

    fun saveToken(tokenResponse: TokenResponse) {
        FileWriter(TOKEN_FILE_NAME).use { writer ->
            GsonBuilder().create().toJson(tokenResponse, writer)
        }
    }

    @Throws(RuntimeException::class)
    fun getToken(): String {

        val reader = JsonReader(FileReader(TOKEN_FILE_NAME))
        val tokenResponse = GsonBuilder().create().fromJson<TokenResponse>(reader, TokenResponse::class.java)
        return tokenResponse.accessToken
    }

}