package com.example.happyplaces.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.happyplaces.R
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.databinding.ActivityHappyPlaceDetailBinding
import com.example.happyplaces.fragments.AddFragment
import com.example.happyplaces.fragments.HomeFragment
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HappyPlaceDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private var binding: ActivityHappyPlaceDetailBinding? = null
    var happyPlaceDetailModel: PlaceEntity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.collapsingToolbar?.setExpandedTitleTypeface(
            Typeface.create(binding?.collapsingToolbar?.getExpandedTitleTypeface(), Typeface.BOLD))

        if (intent.hasExtra(HomeFragment.EXTRA_PLACE_DETAILS)) {
            happyPlaceDetailModel =
                intent.getParcelableExtra(HomeFragment.EXTRA_PLACE_DETAILS) as PlaceEntity?
        }

        if (happyPlaceDetailModel != null) {
            setSupportActionBar(binding?.toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            binding?.toolbar?.setNavigationOnClickListener {
                onBackPressed()
            }

            setValues(happyPlaceDetailModel!!)
            setMapFragment()
        }

        binding?.fabEdit?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(HomeFragment.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
            startActivity(intent)
            finish()
        }

        binding?.llPhone?.setOnClickListener {
            val number = happyPlaceDetailModel!!.phoneNumber!!.trim()
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel: " + Uri.encode(number)))
            startActivity(intent)
        }
        binding?.llWebsite?.setOnClickListener {
            val url = happyPlaceDetailModel!!.website!!.trim()
            val intent = Intent(Intent.ACTION_VIEW)

            if (url.contains("https://"))
                intent.data = Uri.parse(url)
            else
                intent.data = Uri.parse("https://$url")

            startActivity(intent)
        }
        binding?.llEmail?.setOnClickListener {
            val addresses = happyPlaceDetailModel?.emailAddress?.split(",".toRegex())?.toTypedArray()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, addresses)
            }
            startActivity(intent)
        }

    }

    private fun setMapFragment() {
        val supportMapsFragment: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapsFragment.getMapAsync(this)
    }

    @SuppressLint("SetTextI18n")
    private fun setValues(place: PlaceEntity) {
        // Mandatory Fields
        binding?.tvTitle?.text = place.title
        binding?.ivImage?.setImageURI(Uri.parse(place.image))
        binding?.tvDate?.text = place.date
        binding?.description?.text = place.description

        // Ratings
        binding?.ratingBar?.rating = place.rating
        binding?.rating?.text = place.rating.toString()

        // Is Favorite or not
        if (place.isFavorite == 0)
            binding?.btnLike?.setImageResource(R.drawable.heart_not_liked)
        else
            binding?.btnLike?.setImageResource(R.drawable.heart_liked)

        // Set Primary Address in primary details
        if (place.state!!.isNotBlank() && place.country!!.isNotBlank()) {
            binding?.tvStateCountry?.text = "${place.state}, ${place.country}"
        } else {
            binding?.tvStateCountry?.visibility = View.GONE
        }

        // Set up Category
        if (place.category!!.isNotBlank()) {
            binding?.ivCategoryImage?.setImageResource(getCategoryIcon(place))
            binding?.tvCategory?.text = place.category
        }
        else
            binding?.llCategory?.visibility = View.GONE

        // Contacts
        if (place.phoneNumber!!.isNotBlank()
            || place.emailAddress!!.isNotBlank()
            || place.website!!.isNotBlank()) {
            setupContacts(place)
        } else {
            binding?.llContacts?.visibility = View.GONE
        }

        // Address
        if (place.streetAddress1!!.isNotBlank()
            || place.streetAddress2!!.isNotBlank()
            || place.state.isNotBlank()
            || place.country!!.isNotBlank()
            || place.zip!!.isNotBlank()) {
            setupAddress(place)
        } else {
            binding?.llAddress?.visibility = View.GONE
        }

    }

    private fun setupAddress(place: PlaceEntity) {
        if (place.streetAddress1!!.isNotBlank())
            binding?.firstAddress?.text = place.streetAddress1
        else
            binding?.firstAddress?.visibility = View.GONE

        if (place.streetAddress2!!.isNotBlank())
            binding?.secondAddress?.text = place.streetAddress2
        else
            binding?.secondAddress?.visibility = View.GONE

        if (place.state!!.isNotBlank()
            && place.country!!.isNotBlank()
            && place.zip!!.isNotBlank())
            binding?.thirdAddress?.text = "${place.state}-${place.zip}, ${place.country}"
        else if (place.state.isNotBlank()
                && place.country!!.isNotBlank()
                && place.zip!!.isBlank())
            binding?.thirdAddress?.text = "${place.state}, ${place.country}"
        else if(place.state.isNotBlank()
                && place.country!!.isBlank()
                && place.zip!!.isNotBlank())
            binding?.thirdAddress?.text = "${place.state}-${place.zip}"
        else if (place.state.isBlank()
                && place.country!!.isNotBlank()
                && place.zip!!.isNotBlank())
            binding?.thirdAddress?.text = "${place.zip}, ${place.country}"
        else if (place.state.isNotBlank()
                && place.country!!.isBlank()
                && place.zip!!.isBlank())
            binding?.thirdAddress?.text = "${place.state}"
        else if (place.state.isBlank()
            && place.country!!.isNotBlank()
            && place.zip!!.isBlank())
            binding?.thirdAddress?.text = "${place.country}"
        else if (place.state.isBlank()
            && place.country!!.isBlank()
            && place.zip!!.isNotBlank())
            binding?.thirdAddress?.text = "${place.zip}"
        else
            binding?.thirdAddress?.visibility = View.GONE
    }

    private fun setupContacts(place: PlaceEntity) {
        if (place.phoneNumber!!.isNotBlank())
            binding?.tvPhone?.text = place.phoneNumber
        else
            binding?.llPhone?.visibility = View.GONE

        if (place.emailAddress!!.isNotBlank())
            binding?.tvEmail?.text = place.emailAddress
        else
            binding?.llEmail?.visibility = View.GONE

        if (place.website!!.isNotBlank())
            binding?.tvWebsite?.text = place.website
        else
            binding?.llWebsite?.visibility = View.GONE
    }

    private fun getCategoryIcon(place: PlaceEntity): Int {
        return when (place.category) {
            "Food & drink" -> R.drawable.food_and_drink
            "Shopping" -> R.drawable.shopping
            "Services" -> R.drawable.services
            "Hotels & lodging" -> R.drawable.hotel_and_lodging
            "Outdoors & recreation" -> R.drawable.outdoors_and_recreation
            "Religion" -> R.drawable.religion
            "Office & industrial" -> R.drawable.office_and_industrial
            "Residential" -> R.drawable.residential
            else -> R.drawable.education
        }
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Add Marker For Location
        val position = LatLng(happyPlaceDetailModel!!.latitude, happyPlaceDetailModel!!.longitude)
        googleMap.addMarker(MarkerOptions().position(position).title(happyPlaceDetailModel!!.location))
        // Add Zoom Feature on location
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }

}