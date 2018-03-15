package network

import domain.SegmentData
import domain.TokenResponse
import java.awt.print.Book
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit


class SegmentRepository : IbbRepository() {

    var tryout = 1

    @Throws(Exception::class)
    fun getSegments(token: String? = null): Array<SegmentData>? {
        return try {
            val segmentDataResource = getRetrofit(SEGMENT_URL).create(SegmentService::class.java)
            val theToken = token ?: fileUtil.getToken()
            println("$BASE_URL bekleniyor")
            val segmentList = segmentDataResource.getSegments(theToken).execute()
            return segmentList.body()
        } catch (e: Exception) {
            when (tryout) {
                in 1..3 -> {
                    println("Token bulunamadi, tekrar token alinmaya calisiliyor: $tryout. deneme")
                    tryout++
                    return getSegments(tokenRepository.getToken().accessToken)
                }
            }
            tryout = 1
            println("Token alinamiyor...")
            println("Islem sonlandirildi")
            System.exit(-1)
            null
        }
    }
}