package com.raywenderlich.placebook.ui



import android.content.Intent
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.raywenderlich.placebook.R
import com.raywenderlich.placebook.adapter.BookMarkInfoWindowAdaptor
import com.raywenderlich.placebook.adapter.BookmarkListAdapter
import com.raywenderlich.placebook.databinding.ActivityMapsBinding
import com.raywenderlich.placebook.repository.BookmarkRepo
import com.raywenderlich.placebook.viewmodel.MapsViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    //private  var locationRequest: LocationRequest? = null
    private var markers: HashMap<Long,Marker> = HashMap()
    private lateinit var databinding:ActivityMapsBinding
    private lateinit var bookmarkListAdapter: BookmarkListAdapter

    private val mapsViewModel by viewModels<MapsViewModel>()
    private lateinit var mMap: GoogleMap
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        databinding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(databinding.root)



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setUpLocationClient()
        setToolbar()
        setUpPlacesClient()
        setUpNavigationDrawer()
    }

    private fun setUpNavigationDrawer(){
        bookmarkListAdapter = BookmarkListAdapter(null,this)
        databinding.drawerViewMaps.bookmarkRecyclerView.adapter = bookmarkListAdapter

    }

    private fun setToolbar(){
        setSupportActionBar(databinding.mainMapView.toolbar)
        val toggle = ActionBarDrawerToggle(this, databinding.drawerLayout,
            databinding.mainMapView.toolbar, R.string.open_drawer, R.string.close_drawer)

        toggle.syncState()
    }

    fun moveToBookmark(bookmark: MapsViewModel.BookmarkMarkerView){
        databinding.drawerLayout.closeDrawer(databinding.drawerViewMaps.drawerView)
        val marker = markers[bookmark.id]
        marker?.showInfoWindow()

        val location = Location("")

        location.longitude = bookmark.location.longitude
        location.latitude = bookmark.location.latitude

        updateMaplocation(location)
    }

    private fun startDetailActivity(bookmarkId:Long){
        val intent = Intent(this,DetailActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID,bookmarkId)
        startActivity(intent)
    }

    private fun requestLocationPermissions() {
        Log.d(TAG,"5")
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }


    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG,"4")
        mMap = googleMap
        setMapListeners()
        createBookmarkMarkerObserver()
        getCurrentLocation()

        // Add a marker in Sydney and move the camera


    }

    private fun createBookmarkMarkerObserver(){
        mapsViewModel.getBookmarksMarkerViews()?.observe(this){
            mMap.clear()
            markers.clear()
            it?.let {
                displayAllBookmarks(it)
                bookmarkListAdapter.setBookmarkData(it)
            }
        }
    }

    private fun updateMaplocation(location: Location){
        val latlng = LatLng(location.latitude, location.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,16.0f))
    }

    private fun displayAllBookmarks(bookmark: List<MapsViewModel.BookmarkMarkerView>){
        bookmark.forEach{
            addPlaceMarker(it)
        }
    }

    private fun addPlaceMarker(bookmark: MapsViewModel.BookmarkMarkerView): Marker? {
        val marker = mMap.addMarker(MarkerOptions()
            .position(bookmark.location)
            .alpha(0.8f)
            .title(bookmark.phone)
            .snippet(bookmark.name)
            .icon(bookmark.categoryResourceId?.let {
                BitmapDescriptorFactory.fromResource(it)
            })
            .alpha(0.8f)
        )
        marker?.tag  = bookmark
        bookmark.id?.let {
            markers.put(it, marker?:Marker(null))
        }
        return marker
    }

    private fun handleInfoWindowClick(marker: Marker){

        when(marker.tag){
            is PlaceInfo ->{
                var placeInfo = (marker.tag as? PlaceInfo)

                if(placeInfo?.place != null){
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place!!,placeInfo.image)
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarkMarkerView -> {
                val bookmarkMarkerView = (marker.tag as MapsViewModel.BookmarkMarkerView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startDetailActivity(it)
                }
            }
        }

    }

    private fun setMapListeners(){
        mMap.setInfoWindowAdapter(BookMarkInfoWindowAdaptor(this))
        mMap.setOnPoiClickListener {
            displayPoi(it)
        }
        mMap.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest){
        displayPoiGetPoiStep(pointOfInterest)
    }

   private fun displayPoiDisplayStep(place: Place, photo: Bitmap?){
       /*
        val iconPhoto = if(photo == null){
            BitmapDescriptorFactory.defaultMarker()
        }else{
            BitmapDescriptorFactory.fromBitmap(photo)
        }
*/
        val marker = mMap.addMarker(MarkerOptions()
           .position(place.latLng as LatLng)
           //.icon(iconPhoto)
           .title(place.name)
           .snippet(place.phoneNumber)
       )
       marker?.tag = PlaceInfo(place,photo)
       marker?.showInfoWindow()

    }


    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetaData = place.photoMetadatas?.get(0)

        if (photoMetaData == null) {
            displayPoiDisplayStep(place, null)
            return
        }

        val photoRequest = FetchPhotoRequest.builder(photoMetaData)
            .setMaxWidth(
                resources
                    .getDimensionPixelSize(R.dimen.default_image_width)
            )
            .setMaxHeight(resources.getDimensionPixelSize(R.dimen.default_image_height))
            .build()

        placesClient.fetchPhoto(photoRequest)
            .addOnSuccessListener {
                val bitmap = it?.bitmap
                displayPoiDisplayStep(place, bitmap)

            }.addOnFailureListener {exception ->
                val exception = exception
                if (exception is ApiException){
                    val statusCode = exception.statusCode

                    Log.e(TAG,"place not found".plus(exception.message) + statusCode)
                }
            }
    }
    private fun displayPoiGetPoiStep(pointOfInterest: PointOfInterest){
        val placeId = pointOfInterest.placeId

        val placeFields = listOf(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES

        )

        val request = FetchPlaceRequest
            .builder(placeId,placeFields)
            .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener {
                Log.d(TAG,Thread.currentThread().name)
                val place = it.place
             displayPoiGetPhotoStep(place)
            }.addOnFailureListener { exception ->
                val exception = exception
                if (exception is ApiException){
                    val statusCode = exception.statusCode

                    Log.e(TAG,"place not found".plus(exception.message) + statusCode)
                }
            }
    }


    private fun setUpPlacesClient(){
        Places.initialize(applicationContext,getString(R.string.api_key))
        placesClient = Places.createClient(this)
    }

    private fun setUpLocationClient(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun getCurrentLocation(){

        if (ActivityCompat.checkSelfPermission(
                this,android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG,"2")
            requestLocationPermissions()
        }else{
/*
            if (locationRequest == null ){
                locationRequest = LocationRequest.create()

                locationRequest?.let{ locationRequest ->
                    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    locationRequest.interval = 5000
                    locationRequest.fastestInterval = 1000
                    val locationCallback = object: LocationCallback(){
                        override fun onLocationResult(p0: LocationResult) {
                            getCurrentLocation()
                        }
                    }
                    fusedLocationClient.requestLocationUpdates(locationRequest,locationCallback,null)
                }

            }

 */

            Log.d(TAG,"3")

            mMap.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null){

                    val latLong = LatLng(location.latitude,location.longitude)
                   /*
                    mMap.clear()
                    mMap.addMarker(MarkerOptions().position(latLong).title("you are here!"))
*/
                    val update = CameraUpdateFactory.newLatLngZoom(latLong,16f)

                    mMap.moveCamera(update)
                }else{
                    Log.e(TAG, "no location found")
                }

            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION){
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG,"1")
                getCurrentLocation()
            }
        }else{
            Log.e(TAG,"location permission denied")
        }
    }

    companion object{
        const val EXTRA_BOOKMARK_ID = "com.raywenderlich.placebook.EXTRA_BOOKMARK_ID"
        const val REQUEST_LOCATION = 1
        const val TAG = "thisActivity"
    }

class PlaceInfo(val place:Place? = null,
                val image:Bitmap? = null
                )

}