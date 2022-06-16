package com.example.happyplaces.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlaceAdapter
import com.example.happyplaces.database.PlaceApp
import com.example.happyplaces.database.PlaceDao
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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
                setupHappyPlacesListIntoAdapter(list, placeDao)
            }
        }

        return binding?.root
    }

    private fun setupHappyPlacesListIntoAdapter(placesList: ArrayList<PlaceEntity>,
                                                placeDao: PlaceDao) {
        if (placesList.isNotEmpty()) {
            val itemAdapter=  HappyPlaceAdapter(requireContext(), placesList)

            binding?.rvList?.layoutManager = LinearLayoutManager(requireContext())
            binding?.rvList?.adapter = itemAdapter
            binding?.rvList?.visibility = View.VISIBLE
            binding?.ivNoItem?.visibility = View.INVISIBLE
            binding?.tvNoItem?.visibility = View.INVISIBLE

        } else {
            binding?.rvList?.visibility = View.INVISIBLE
            binding?.ivNoItem?.visibility = View.VISIBLE
            binding?.tvNoItem?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

}