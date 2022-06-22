package com.example.happyplaces.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.happyplaces.R
import com.example.happyplaces.activities.HappyPlaceDetailActivity
import com.example.happyplaces.adapters.FavoritePlaceAdapter
import com.example.happyplaces.database.PlaceApp
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.databinding.FragmentFavoriteBinding
import com.example.happyplaces.utils.CenterZoom
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FavoriteFragment : Fragment() {

    private var binding: FragmentFavoriteBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFavoriteBinding.inflate(layoutInflater)

        // Set StatusBar theme
        val window: Window = requireActivity().window
        window.statusBarColor = ContextCompat.getColor(requireContext(), R.color.like)
        window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.like)

        // Recycler View
        val layoutManager = CenterZoom(requireContext())
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = true
        binding?.rvList?.layoutManager = layoutManager

        // Auto Centre View
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding?.rvList)
        binding?.rvList?.isNestedScrollingEnabled = false

        // Add Item
        val placeDao = (requireActivity().application as PlaceApp).db.placeDao()
        lifecycleScope.launch {
            placeDao.getFavoritePlaces().collect {
                val list = ArrayList(it)

                val itemAdapter = FavoritePlaceAdapter(requireContext(), list)
                binding?.rvList?.adapter = itemAdapter

                itemAdapter.setOnClickListener(object : FavoritePlaceAdapter.OnClickListener {
                    override fun onClick(position: Int, model: PlaceEntity) {
                        val intent = Intent(requireContext(), HappyPlaceDetailActivity::class.java)
                        intent.putExtra(HomeFragment.EXTRA_PLACE_DETAILS, model)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                })
            }
        }

        return binding?.root
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

}