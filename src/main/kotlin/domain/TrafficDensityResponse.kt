package tr.gov.ibb.ibbceptrafik.domain.response

import com.google.gson.annotations.SerializedName

class TrafficDensityResponse(
    @SerializedName("FlowDate") val flowDate: String,
    @SerializedName("Boundary") val boundary: Boundary,
    @SerializedName("ZoomLevel") val zoomLevel: Int,
    @SerializedName("MinRoadTypeLevel") val minRoadTypeLevel: Int,
    @SerializedName("MaxRoadTypeLevel") val maxRoadTypeLevel: Int,
    @SerializedName("FCDIntensityList") val intensityList: ArrayList<Intensity>
)

data class Boundary(
    @SerializedName("x1") val x1: String,
    @SerializedName("y1") val y1: String,
    @SerializedName("x2") val x2: String,
    @SerializedName("y2") val y2: String
)

data class Intensity(
    @SerializedName("FCDSegmentId") val segmentId: Int,
    @SerializedName("Speed") val speed: Int,
    @SerializedName("ColorIndex") val colorIndex: Int
)
