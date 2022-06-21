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

class HappyPlaceDetailActivity : AppCompatActivity() {

    private var binding: ActivityHappyPlaceDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.collapsingToolbar?.setExpandedTitleTypeface(
            Typeface.create(binding?.collapsingToolbar?.getExpandedTitleTypeface(), Typeface.BOLD))

        var happyPlaceDetailModel: PlaceEntity? = null
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

            setValues(happyPlaceDetailModel)
        }

        binding?.fabEdit?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(HomeFragment.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
            startActivity(intent)
            finish()
        }

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

}