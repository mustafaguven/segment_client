package file

import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import domain.TokenResponse
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.nio.file.Paths

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
        return "Bearer ${tokenResponse.accessToken}"
    }

    fun deleteDataFolder() {
        deleteDir(File("${Paths.get(".").toAbsolutePath().normalize()}/data"))
    }


    fun deleteDir(file: File) {
        val contents = file.listFiles()
        if (contents != null) {
            for (f in contents) {
                deleteDir(f)
            }
        }
        file.delete()
    }

}