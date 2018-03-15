package entry

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import couchbase.CouchbaseUtil
import domain.SegmentDataIn
import domain.SegmentMap
import file.FileUtil
import network.SegmentRepository
import tr.gov.ibb.ibbceptrafik.domain.response.Intensity
import tr.gov.ibb.ibbceptrafik.domain.response.TrafficDensityResponse
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStreamReader

private val segmentListToken = object : TypeToken<Array<SegmentDataIn>>() {}.type
var PATH = "src/main/kotlin/assets/intensity_son.json"
val couchbaseUtil: CouchbaseUtil by lazy { CouchbaseUtil() }
val segmentRepository by lazy { SegmentRepository() }
val fileUtil by lazy { FileUtil() }

val addedRoadTypes = mutableListOf<Int>()
var creationType: CreationType = CreationType.ALL_IN_ONE_DOCUMENT

fun main(args: Array<String>) {
    createIntensityMock()

    if (args.isNotEmpty()) {
        when (args[0]) {
            "FILE" -> {
                PATH = if (args.size >= 2) {
                    args[1]
                } else {
                    PATH
                }

                if (args.size == 3) {
                    creationType = CreationType.valueOf(args[2])
                }
                createDBForMapViaLocalPath()
            }
            "REST"
            -> {
                if (args.size == 2) {
                    creationType = CreationType.valueOf(args[1])
                }
                createDBFromRest()
            }
        }
    } else {
        createDBFromRest()
    }
}

fun createIntensityMock() {
    val file = File(PATH)
    val type = object : TypeToken<TrafficDensityResponse>() {}.type
    val inputStream = FileInputStream(file)
    val streamReader = InputStreamReader(inputStream, Charsets.UTF_8)
    val response: TrafficDensityResponse = Gson().fromJson(JsonReader(streamReader), type)


    //val set = mutableSetOf<Intensity>()
    val map = mutableMapOf<Int, Intensity>()
    val preCount = response.intensityList.size

    println("toplam intensity list: $preCount")

    for (i in response.intensityList) {
        map[i.segmentId] = i
    }
    println("intensity list uniquelestirildi: ${map.size}")

    response.intensityList.clear()
    for (i in map) {
        response.intensityList.add(i.value)
    }
    println("prune edilen kayit sayisi: ${preCount - response.intensityList.size}")

    FileWriter("intensity_unique_response.json").use { writer ->
        GsonBuilder().create().toJson(response, writer)
    }
}

fun createDBFromRest() {
    addedRoadTypes.clear()
    fileUtil.deleteDataFolder()

    println("Segment Datasi cekiliyor")
    val segmentList = segmentRepository.getSegments()
    println("Segment datasi alindi, map e ceviriliyor")

    segmentList!!.sortBy { it.roadType }
    println("RoadType'a gore yeniden siralandi")

/*    var j = 0
    for (i in segmentList){
        if(i.roadType == 1000) j++

    }
    println("$j adet 1000 kayit")*/

    when (creationType) {
        CreationType.BY_ROAD_TYPE -> createDBByRoadType(segmentList)
        CreationType.BY_ZOOM_LEVEL -> createDBByZoomLevel(segmentList)
        CreationType.ALL_IN_ONE_DOCUMENT -> createDBAllInOneDocument(segmentList)
    }

    println("Islem tamamlandi")
    System.exit(0)
}


fun createDBByZoomLevel(segmentList: Array<SegmentDataIn>) {
    val zoomLevels = arrayOf(SegmentMap(), SegmentMap(), SegmentMap())
    for (i in segmentList) {
        if (i.roadType == 1010 || i.roadType == 1020 || i.roadType == 1030 || i.roadType == 1040) {
            zoomLevels[0][i.segmentId] = i
        } else if (i.roadType == 1050) {
            zoomLevels[1][i.segmentId] = i
        } else if (i.roadType == 1060) {
            zoomLevels[2][i.segmentId] = i
        }
    }

    couchbaseUtil.upsertSegmentDocument(1, zoomLevels[0])
    println("1 nolu zoomLevela ${zoomLevels[0].size} adet segment eklendi")
    couchbaseUtil.upsertSegmentDocument(2, zoomLevels[1])
    println("2 nolu zoomLevela ${zoomLevels[1].size} adet segment eklendi")
    couchbaseUtil.upsertSegmentDocument(3, zoomLevels[2])
    println("3 nolu zoomLevela ${zoomLevels[2].size} adet segment eklendi")
}

//1000 and 1070 are not include
private fun createDBByRoadType(segmentList: Array<SegmentDataIn>) {
    val segmentMap = SegmentMap()
    for (i in segmentList) {
        if (i.roadType == 1000) continue

        if (!addedRoadTypes.contains(i.roadType)) {
            addedRoadTypes.add(i.roadType)

            if (addedRoadTypes.size > 1) {
                addToCouchbaseLiteByRoadType(addedRoadTypes.size - 2, segmentMap)
            }
        }

        segmentMap[i.segmentId] = i
    }
}

private fun addToCouchbaseLiteByRoadType(roadTypeIndex: Int, segmentMap: SegmentMap) {
    couchbaseUtil.upsertSegmentDocument(addedRoadTypes[roadTypeIndex], segmentMap)
    println("${addedRoadTypes[roadTypeIndex]} nolu roadType dokumanina ${segmentMap.size} adet segment eklendi")
    segmentMap.clear()

}

private fun createDBAllInOneDocument(segmentList: Array<SegmentDataIn>?) {
    val segmentMap = SegmentMap()
    for (i in segmentList!!) {
        if (i.roadType == 1000 || i.roadType == 1070) continue
        segmentMap[i.segmentId] = i
    }
    println("toplam adet ${segmentMap.size}")
    couchbaseUtil.upsertSegmentDocument(1, segmentMap)
}

//simple db creation through a file path
private fun createDBViaLocalPath() {
    val inputStream = FileInputStream(File(PATH))
    val streamReader = InputStreamReader(inputStream, Charsets.UTF_8)
    val segmentList: Array<SegmentDataIn> = Gson().fromJson(JsonReader(streamReader), segmentListToken)

    for (i in segmentList) {
        println("${i.segmentId} eklendi")
        couchbaseUtil.upsertSegmentDocument(i)
    }
}

//db creation through a file path but first it converts list to map
private fun createDBForMapViaLocalPath() {
    val inputStream = FileInputStream(File(PATH))
    val streamReader = InputStreamReader(inputStream, Charsets.UTF_8)
    val segmentList: Array<SegmentDataIn> = Gson().fromJson(JsonReader(streamReader), segmentListToken)

    val segmentMap = SegmentMap()
    for (i in segmentList) {
        segmentMap[i.segmentId] = i
    }

    couchbaseUtil.upsertSegmentDocument(1, segmentMap)
}

enum class CreationType { BY_ZOOM_LEVEL, BY_ROAD_TYPE, ALL_IN_ONE_DOCUMENT }



