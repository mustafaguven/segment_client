package domain

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable
import java.io.Serializable

data class SegmentData(
        @SerializedName("FCDSegmentId") val segmentId: Int = 0,
        @SerializedName("RoadType") var roadType: Int = 0,
        @SerializedName("Coords") var coordinates: String = ""
        //,
        //@SerializedName("Action") val action: String = "",
        //@SerializedName("FCDProviderId") val providerId: Int = 0
        ) : Serializable {

/*
    companion object {
        const val SEMICOLON: String = ";"
        const val SPACE: String = " "
    }

  fun updatePointList(): List<TheLatLng> {
        val list = mutableListOf<TheLatLng>()
        val points: List<String> = coordinates.removeSuffix(SEMICOLON).split(SEMICOLON)
        Observable.fromIterable(points).subscribe({
            val p = it.split(SPACE)
            list.add(TheLatLng(p[1].toDouble(), p[0].toDouble()))
        })
        return list
    }*/
}