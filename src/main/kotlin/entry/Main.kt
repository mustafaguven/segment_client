package entry

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import couchbase.CouchbaseUtil
import domain.SegmentData
import domain.SegmentMap
import file.FileUtil
import network.SegmentRepository
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

private val segmentListToken = object : TypeToken<Array<SegmentData>>() {}.type
var PATH = "src/entry.main/assets/segments.json"
val couchbaseUtil: CouchbaseUtil by lazy { CouchbaseUtil() }
val segmentRepository by lazy { SegmentRepository() }
val fileUtil by lazy { FileUtil() }

val addedRoadTypes = mutableListOf<Int>()
var creationType: CreationType = CreationType.BY_ROAD_TYPE

fun main(args: Array<String>) {
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

fun createDBFromRest() {

    addedRoadTypes.clear()
    fileUtil.deleteDataFolder()

    println("Segment Datasi cekiliyor")
    val segmentList = segmentRepository.getSegments()
    println("Segment datasi alindi, map e ceviriliyor")

    segmentList!!.sortBy { it.roadType }
    println("RoadType'a gore yeniden siralandi")

    when(creationType){
        CreationType.BY_ROAD_TYPE -> createDBByRoadType(segmentList)
        CreationType.BY_ZOOM_LEVEL -> createDBByZoomLevel(segmentList)
    }


    println("Islem tamamlandi")
    System.exit(0)
}

fun createDBByZoomLevel(segmentList: Array<SegmentData>) {
    val segmentMap = SegmentMap()
    for (i in segmentList) {
        if (!addedRoadTypes.contains(i.roadType)) {
            addedRoadTypes.add(i.roadType)

            if (addedRoadTypes.size > 1) {
                addToCouchbaseLiteByRoadType(addedRoadTypes.size - 2, segmentMap)
            }
        }
        segmentMap[i.segmentId] = i
    }
}

private fun createDBByRoadType(segmentList: Array<SegmentData>) {
    val segmentMap = SegmentMap()
    for (i in segmentList) {
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


//simple db creation through a file path
private fun createDBViaLocalPath() {
    val inputStream = FileInputStream(File(PATH))
    val streamReader = InputStreamReader(inputStream, Charsets.UTF_8)
    val segmentList: Array<SegmentData> = Gson().fromJson(JsonReader(streamReader), segmentListToken)

    for (i in segmentList) {
        println("${i.segmentId} eklendi")
        couchbaseUtil.upsertSegmentDocument(i)
    }
}

//db creation through a file path but first it converts list to map
private fun createDBForMapViaLocalPath() {
    val inputStream = FileInputStream(File(PATH))
    val streamReader = InputStreamReader(inputStream, Charsets.UTF_8)
    val segmentList: Array<SegmentData> = Gson().fromJson(JsonReader(streamReader), segmentListToken)

    val segmentMap = SegmentMap()
    for (i in segmentList) {
        segmentMap[i.segmentId] = i
    }

    couchbaseUtil.upsertSegmentDocument(1, segmentMap)

}

enum class CreationType { BY_ZOOM_LEVEL, BY_ROAD_TYPE }



