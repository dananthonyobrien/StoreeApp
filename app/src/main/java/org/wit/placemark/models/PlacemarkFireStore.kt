package org.wit.placemark.models

import android.content.Context
import android.graphics.Bitmap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import org.wit.placemark.readImageFromPath
import timber.log.Timber.i
import java.io.ByteArrayOutputStream
import java.io.File

class PlacemarkFireStore(val context: Context) : PlacemarkStore {
    val placemarks = ArrayList<PlacemarkModel>()
    lateinit var userId: String
    lateinit var db: DatabaseReference
    lateinit var st: StorageReference
    override suspend fun findAll(): List<PlacemarkModel> {
        return placemarks
    }

    override suspend fun findById(id: Long): PlacemarkModel? {
        val foundPlacemark: PlacemarkModel? = placemarks.find { p -> p.id == id }
        return foundPlacemark
    }

    override suspend fun create(placemark: PlacemarkModel) {
        val key = db.child("users").child(userId).child("placemarks").push().key
        key?.let {
            placemark.fbId = key
            placemarks.add(placemark)
            db.child("users").child(userId).child("placemarks").child(key).setValue(placemark)
            updateImage(placemark)
        }
    }

    override suspend fun update(placemark: PlacemarkModel) {
        var foundPlacemark: PlacemarkModel? = placemarks.find { p -> p.fbId == placemark.fbId }
        if (foundPlacemark != null) {
            foundPlacemark.title = placemark.title
            foundPlacemark.description = placemark.description
            foundPlacemark.image = placemark.image
            foundPlacemark.location = placemark.location
        }

        db.child("users").child(userId).child("placemarks").child(placemark.fbId).setValue(placemark)
        if(placemark.image.length > 0){
            updateImage(placemark)
        }
    }

    override suspend fun delete(placemark: PlacemarkModel) {
        db.child("users").child(userId).child("placemarks").child(placemark.fbId).removeValue()
        placemarks.remove(placemark)
    }

    override suspend fun clear() {
        placemarks.clear()
    }

    fun fetchPlacemarks(placemarksReady: () -> Unit) {
        val valueEventListener = object : ValueEventListener {
            override fun onCancelled(dataSnapshot: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot!!.children.mapNotNullTo(placemarks) {
                    it.getValue<PlacemarkModel>(
                        PlacemarkModel::class.java
                    )
                }
                placemarksReady()
            }
        }
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        st = FirebaseStorage.getInstance().reference
        db = FirebaseDatabase.getInstance("https://storeeapp-941d6-default-rtdb.firebaseio.com/").reference
        placemarks.clear()
        db.child("users").child(userId).child("placemarks")
            .addListenerForSingleValueEvent(valueEventListener)
    }
    fun updateImage(placemark: PlacemarkModel){
        if(placemark.image != ""){
            val fileName = File(placemark.image)
            val imageName = fileName.getName()

            var imageRef = st.child(userId + '/' + imageName)
            val baos = ByteArrayOutputStream()
            val bitmap = readImageFromPath(context, placemark.image)

            bitmap?.let {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)

                val data = baos.toByteArray()
                val uploadTask = imageRef.putBytes(data)

                uploadTask.addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        placemark.image = it.toString()
                        db.child("users").child(userId).child("placemarks").child(placemark.fbId).setValue(placemark)
                    }
                }.addOnFailureListener{
                    var errorMessage = it.message
                    i("Failure: $errorMessage")
                }
            }

        }
    }
}