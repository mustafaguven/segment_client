package network

import domain.SegmentData
import file.FileUtil
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers


interface SegmentService {
    @Headers("Accept: application/json")
    @GET("FCDSegments")
    fun getSegments(@Header("Authorization") token: String): Call<Array<SegmentData>>
}