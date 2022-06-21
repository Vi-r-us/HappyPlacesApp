package com.example.happyplaces.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.R
import com.example.happyplaces.activities.HappyPlaceDetailActivity
import com.example.happyplaces.adapters.HappyPlaceAdapter
import com.example.happyplaces.database.PlaceApp
import com.example.happyplaces.database.PlaceDao
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.databinding.FragmentHomeBinding
import com.example.happyplaces.utils.SwipeToDeleteCallback
import com.example.happyplaces.utils.SwipeToEditCallback
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Set StatusBar theme
        val window: Window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.home)
        window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.home)

        // Setting the RecyclerView
        val placeDao = (requireActivity().application as PlaceApp).db.placeDao()
        lifecycleScope.launch {
            placeDao.fetchAllPlaces().collect {
                val list = ArrayList(it)
                list.reverse()
                setupHappyPlacesListIntoAdapter(list, placeDao)
            }
        }

        return binding?.root
    }

    private fun setupSearchViewAdapter(
        placesList: ArrayList<PlaceEntity>,
        placeDao: PlaceDao,
        itemAdapter: HappyPlaceAdapter
    ) {
        binding?.svSearchbar?.clearFocus()
        binding?.svSearchbar?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredPlaceList = ArrayList<PlaceEntity>()

                placesList.forEach {
                    if (it.title!!.lowercase(Locale.getDefault())!!
                            .contains(newText!!.lowercase(Locale.getDefault())))
                        filteredPlaceList.add(it)
                }

                if (filteredPlaceList.isNotEmpty())
                    itemAdapter.setFilteredList(filteredPlaceList)
                else
                    Toast.makeText(requireContext(), "Data Not Found", Toast.LENGTH_SHORT).show()
                itemAdapter.notifyDataSetChanged()
                return false
            }
        })

    }

    private fun setupHappyPlacesListIntoAdapter(placesList: ArrayList<PlaceEntity>,
                                                placeDao: PlaceDao) {
        if (placesList.isNotEmpty()) {
            val itemAdapter =  HappyPlaceAdapter(requireContext(), placesList,
                { place ->
                    updateIsFavorite(place, placeDao) })

            setupSearchViewAdapter(placesList, placeDao, itemAdapter)

            binding?.rvList?.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false)
            binding?.rvList?.adapter = itemAdapter
            binding?.rvList?.smoothScrollToPosition(0)

            itemAdapter.setOnClickListener(object : HappyPlaceAdapter.OnClickListener {
                override fun onClick(position: Int, model: PlaceEntity) {
                    val intent = Intent(requireContext(), HappyPlaceDetailActivity::class.java)
                    intent.putExtra(EXTRA_PLACE_DETAILS, model)
                    startActivity(intent)
                    requireActivity().finish()
                }
            })

            val editSwipeHandler = object : SwipeToEditCallback(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val adapter = binding?.rvList?.adapter as HappyPlaceAdapter
                    adapter.notifyEditItem(requireActivity(), viewHolder.adapterPosition,
                        ADD_PLACE_ACTIVITY_REQUEST_CODE)
                }
            }
            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(binding?.rvList)

            val deleteSwipeHandler = object : SwipeToDeleteCallback(requireContext()) {
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    deletePlace(viewHolder, placeDao)
                }
            }
            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            deleteItemTouchHelper.attachToRecyclerView(binding?.rvList)

            binding?.svSearchbar?.visibility = View.VISIBLE
            binding?.rvList?.visibility = View.VISIBLE
            binding?.ivNoItem?.visibility = View.INVISIBLE
            binding?.tvNoItem?.visibility = View.INVISIBLE

        } else {
            binding?.rvList?.visibility = View.INVISIBLE
            binding?.svSearchbar?.visibility = View.INVISIBLE
            binding?.ivNoItem?.visibility = View.VISIBLE
            binding?.tvNoItem?.visibility = View.VISIBLE
        }
    }

    private fun updateIsFavorite(place: PlaceEntity, placeDao: PlaceDao) {
        // Mandatory Fields
        val cId: Int = place.id
        val cTitle: String? = place.title
        val cImage: String? = place.image
        val cDescription: String? = place.description
        val cDate: String? = place.date
        val cLocation: String? = place.location
        val cLatitude: Double = place.latitude
        val cLongitude: Double = place.longitude

        // Temporary Fields
        val cCategory: String? = place.category
        val cStreetAddress1: String? = place.streetAddress1
        val cStreetAddress2: String? = place.streetAddress2
        val cCountry: String? = place.country
        val cState: String? = place.state
        val cZip: String? = place.zip
        val cRating: Float = place.rating
        var cIsFavorite: Int = place.isFavorite
        val cPhoneNumber: String? = place.phoneNumber
        val cEmailAddress: String? = place.emailAddress
        val cWebsite: String? = place.website

        cIsFavorite = if(cIsFavorite == 0) 1 else 0
        lifecycleScope.launch(Dispatchers.IO) {
            placeDao.update(PlaceEntity(id = cId,
                // Mandatory Fields
                title = cTitle,
                date = cDate,
                description = cDescription,
                location = cLocation,
                image = cImage,
                latitude = cLatitude,
                longitude = cLongitude,

                // Not-Mandatory Fields
                category = cCategory,
                rating = cRating,
                streetAddress1 = cStreetAddress1,
                streetAddress2 = cStreetAddress2,
                country = cCountry,
                state = cState,
                zip = cZip,
                phoneNumber = cPhoneNumber,
                emailAddress = cEmailAddress,
                website = cWebsite,
                isFavorite = cIsFavorite
            ))
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun deletePlace(viewHolder: RecyclerView.ViewHolder, placeDao: PlaceDao) {
        val adapter = binding?.rvList?.adapter as HappyPlaceAdapter
        val items = adapter.removeAt(viewHolder.adapterPosition, placeDao)

        val position = viewHolder.adapterPosition
        // Mandatory Fields
        val cId: Int = items[position].id
        val cTitle: String? = items[position].title
        val cImage: String? = items[position].image
        val cDescription: String? = items[position].description
        val cDate: String? = items[position].date
        val cLocation: String? = items[position].location
        val cLatitude: Double = items[position].latitude
        val cLongitude: Double = items[position].longitude

        // Temporary Fields
        val cCategory: String? = items[position].category
        val cStreetAddress1: String? = items[position].streetAddress1
        val cStreetAddress2: String? = items[position].streetAddress2
        val cCountry: String? = items[position].country
        val cState: String? = items[position].state
        val cZip: String? = items[position].zip
        val cRating: Float = items[position].rating
        val cIsFavorite: Int = items[position].isFavorite
        val cPhoneNumber: String? = items[position].phoneNumber
        val cEmailAddress: String? = items[position].emailAddress
        val cWebsite: String? = items[position].website

        lifecycleScope.launch(Dispatchers.IO) {
            placeDao.delete(items[position])
        }
        adapter.notifyItemRemoved(position)

        Snackbar.make(requireView(), "Place Deleted", Snackbar.LENGTH_LONG)
            .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
            .setTextColor(Color.parseColor("#FFFFFF"))
            .setActionTextColor(Color.parseColor("#FFFFFF"))
            .setBackgroundTint(Color.parseColor("#003fff"))
            .apply {
                view.background =
                    resources.getDrawable(R.drawable.custom_rounded_save_button, null)
            }
            .setAction("Undo") {

                lifecycleScope.launch(Dispatchers.IO) {
                    placeDao.insert(PlaceEntity(id = cId,
                        // Mandatory Fields
                        title = cTitle,
                        date = cDate,
                        description = cDescription,
                        location = cLocation,
                        image = cImage,
                        latitude = cLatitude,
                        longitude = cLongitude,

                        // Not-Mandatory Fields
                        category = cCategory,
                        rating = cRating,
                        streetAddress1 = cStreetAddress1,
                        streetAddress2 = cStreetAddress2,
                        country = cCountry,
                        state = cState,
                        zip = cZip,
                        phoneNumber = cPhoneNumber,
                        emailAddress = cEmailAddress,
                        website = cWebsite,
                        isFavorite = cIsFavorite

                    ))
                }
                adapter.notifyItemInserted(position)

                Toast.makeText(requireContext(), "Place Retrieved", Toast.LENGTH_SHORT).show()
            }.show()

    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    companion object {
        var EXTRA_PLACE_DETAILS = "extra_place_details"
        var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }

}