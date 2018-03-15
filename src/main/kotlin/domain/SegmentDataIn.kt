package domain

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import java.io.Serializable

data class SegmentDataIn(
        @SerializedName("FCDSegmentId") val segmentId: Int = 0,
        @SerializedName("RoadType") var roadType: Int = 0,
        @SerializedName("Coords") var coordinates: String = "") : Serializable
