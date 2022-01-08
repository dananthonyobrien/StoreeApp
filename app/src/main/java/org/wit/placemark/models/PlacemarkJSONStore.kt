package org.wit.placemark.models

import android.content.Context
import android.net.Uri
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import com.google.gson.*
//import com.google.gson.reflect.TypeToken
import org.wit.placemark.helpers.*
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*

const val JSON_FILE = "placemarks.json"
val gsonBuilder: Gson = GsonBuilder().setPrettyPrinting()
    .registerTypeAdapter(Uri::class.java, UriParser())
    .create()
val listType: Type = object : TypeToken<ArrayList<PlacemarkModel>>() {}.type

fun generateRandomId(): Long {
    return Random().nextLong()
}

class PlacemarkJSONStore(private val context: Context) : PlacemarkStore {

    var placemarks = mutableListOf<PlacemarkModel>()

    init {
        if (exists(context, JSON_FILE)) {
            deserialize()
        }
    }

    override suspend fun findAll(): MutableList<PlacemarkModel> {
        logAll()
        return placemarks
    }

    override suspend fun create(placemark: PlacemarkModel) {
        placemark.id = generateRandomId()
        placemarks.add(placemark)
        serialize()
    }


    override suspend fun update(placemark: PlacemarkModel) {
        val placemarksList = findAll() as ArrayList<PlacemarkModel>
        var foundPlacemark: PlacemarkModel? = placemarksList.find { p -> p.id == placemark.id }
        if (foundPlacemark != null) {
            foundPlacemark.title = placemark.title
            foundPlacemark.description = placemark.description
            foundPlacemark.image = placemark.image
            foundPlacemark.location = placemark.location
        }
        serialize()
    }

    override suspend fun delete(placemark: PlacemarkModel) {
        val foundPlacemark: PlacemarkModel? = placemarks.find { it.id == placemark.id }
        placemarks.remove(foundPlacemark)
        serialize()
    }
    override suspend fun findById(id:Long) : PlacemarkModel? {
        val foundPlacemark: PlacemarkModel? = placemarks.find { it.id == id }
        return foundPlacemark
    }

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(placemarks, listType)
        write(context, JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, JSON_FILE)
        placemarks = gsonBuilder.fromJson(jsonString, listType)
    }

    private fun logAll() {
        placemarks.forEach { Timber.i("$it") }
    }
    override suspend fun clear(){
        placemarks.clear()
    }
}

class UriParser : JsonDeserializer<Uri>,JsonSerializer<Uri> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Uri {
        return Uri.parse(json?.asString)
    }

    override fun serialize(
        src: Uri?,
        typeOfSrc: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(src.toString())
    }
}