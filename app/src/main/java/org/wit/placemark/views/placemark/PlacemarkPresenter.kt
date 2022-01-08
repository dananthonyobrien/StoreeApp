package org.wit.placemark.views.placemark

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.wit.placemark.helpers.checkLocationPermissions
import org.wit.placemark.helpers.createDefaultLocationRequest
import org.wit.placemark.main.MainApp
import org.wit.placemark.models.Location
import org.wit.placemark.models.PlacemarkModel
import org.wit.placemark.showImagePicker
import org.wit.placemark.views.location.EditLocationView
import timber.log.Timber
import timber.log.Timber.i

import android.annotation.SuppressLint
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
//import kotlinx.android.synthetic.main.activity_placemark.*
import org.json.JSONObject

class PlacemarkPresenter(private val view: PlacemarkView) {
    private val locationRequest = createDefaultLocationRequest()
    var map: GoogleMap? = null
    var placemark = PlacemarkModel()
    var app: MainApp = view.application as MainApp
    var locationManualyChanged = false;
    //location service
    var locationService: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(view)
    private lateinit var imageIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var mapIntentLauncher : ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    var edit = false;
    private val location = Location(52.245696, -7.139102, 15f)


    init {

        doPermissionLauncher()
        registerImagePickerCallback()
        registerMapCallback()

        if (view.intent.hasExtra("placemark_edit")) {
            edit = true
            placemark = view.intent.extras?.getParcelable("placemark_edit")!!
            view.showPlacemark(placemark)
        }
        else {

            if (checkLocationPermissions(view)) {
                doSetCurrentLocation()
            }
            placemark.location.lat = location.lat
            placemark.location.lng = location.lng
        }

    }


    suspend fun doAddOrSave(title: String, description: String) {
        placemark.title = title
        placemark.description = description
        if (edit) {
            app.placemarks.update(placemark)
        } else {
            app.placemarks.create(placemark)
        }

        view.finish()

    }

    fun doCancel() {
        view.finish()

    }

    suspend fun doDelete() {
        app.placemarks.delete(placemark)
        view.finish()

    }

    fun doSelectImage() {
        showImagePicker(imageIntentLauncher)
    }




    //fun getQuoteURL () {
    //   val queue = Volley.newRequestQueue(this)
    //   val quoteUrl = "https://goquotes-api.herokuapp.com/api/v1/random?count=1"

     //Request a string response from the provided URL.
    //  val stringRequest = StringRequest(Request.Method.GET, quoteUrl, { response ->
     //        textView3.text = "Quote of the Day: ${response}"
      //  },
     //   { textView3.text = "That didn't work!" })

/* Add the request to the RequestQueue. */
    // queue.add(stringRequest)

    // }


    // val stringReq = StringRequest (Request.Method.GET, url,
    //      Response.Listener<String>{response.toString}
    //}





    fun doSetLocation() {
        locationManualyChanged = true;

        if (placemark.location.zoom != 0f) {

            location.lat =  placemark.location.lat
            location.lng = placemark.location.lng
            location.zoom = placemark.location.zoom
            locationUpdate(placemark.location.lat, placemark.location.lng)
        }
        val launcherIntent = Intent(view, EditLocationView::class.java)
            .putExtra("location", location)
        mapIntentLauncher.launch(launcherIntent)
    }

    @SuppressLint("MissingPermission")
    fun doSetCurrentLocation() {

        locationService.lastLocation.addOnSuccessListener {
            locationUpdate(it.latitude, it.longitude)
        }
    }

    @SuppressLint("MissingPermission")
    fun doRestartLocationUpdates() {
        var locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null && locationResult.locations != null) {
                    val l = locationResult.locations.last()
                    if(!locationManualyChanged){
                        locationUpdate(l.latitude, l.longitude)
                    }
                }
            }
        }
        if (!edit) {
            locationService.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }
    fun doConfigureMap(m: GoogleMap) {
        map = m
        locationUpdate(placemark.location.lat, placemark.location.lng)
    }

    fun locationUpdate(lat: Double, lng: Double) {
        placemark.location = location
        map?.clear()
        map?.uiSettings?.setZoomControlsEnabled(true)
        val options = MarkerOptions().title(placemark.title).position(LatLng(placemark.location.lat, placemark.location.lng))
        map?.addMarker(options)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(placemark.location.lat, placemark.location.lng), placemark.location.zoom))
        view.showPlacemark(placemark)
    }

    fun cachePlacemark (title: String, description: String) {
        placemark.title = title;
        placemark.description = description
    }

    private fun registerImagePickerCallback() {

        imageIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when(result.resultCode){
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Result ${result.data!!.data}")
                            placemark.image = result.data!!.data!!.toString()
                            view.updateImage(placemark.image)
                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }

            }
    }

    private fun registerMapCallback() {
        mapIntentLauncher =
            view.registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result ->
                when (result.resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        if (result.data != null) {
                            Timber.i("Got Location ${result.data.toString()}")
                            val location = result.data!!.extras?.getParcelable<Location>("location")!!
                            Timber.i("Location == $location")
                            placemark.location = location
                        } // end of if
                    }
                    AppCompatActivity.RESULT_CANCELED -> { } else -> { }
                }

            }
    }

    private fun doPermissionLauncher() {
        i("permission check called")
        requestPermissionLauncher =
        view.registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted: Boolean ->
            if (isGranted) {
                doSetCurrentLocation()
            } else {
                locationUpdate(location.lat, location.lng)
            }
        }
    }
}