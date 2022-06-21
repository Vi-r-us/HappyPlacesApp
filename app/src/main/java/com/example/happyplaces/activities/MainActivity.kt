package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.happyplaces.R
import com.example.happyplaces.database.PlaceEntity
import com.example.happyplaces.fragments.AddFragment
import com.example.happyplaces.fragments.FavoriteFragment
import com.example.happyplaces.fragments.HomeFragment

class MainActivity : AppCompatActivity() {

    private val addFragment = AddFragment()
    private val favoriteFragment = FavoriteFragment()
    private val homeFragment = HomeFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        replaceFragment(homeFragment)

        findViewById<com.ismaeldivita.chipnavigation.ChipNavigationBar>(R.id.navBar)
            .setOnItemSelectedListener { id ->
                when(id) {
                    R.id.home -> replaceFragment(homeFragment)
                    R.id.add -> replaceFragment(addFragment)
                    R.id.like -> replaceFragment(favoriteFragment)
                }
        }

        if (intent.hasExtra(HomeFragment.EXTRA_PLACE_DETAILS)) {
            goToAddFragmentForEdit(intent.getParcelableExtra<PlaceEntity>(HomeFragment.EXTRA_PLACE_DETAILS))
        }
    }

    private fun goToAddFragmentForEdit(happyPlaceDetailModel: PlaceEntity?) {
        val fragment: Fragment = AddFragment()
        val manager: FragmentManager = supportFragmentManager
        val bundle = Bundle()

        bundle.putInt("REQUEST_CODE", HomeFragment.ADD_PLACE_ACTIVITY_REQUEST_CODE)
        bundle.putParcelable(HomeFragment.EXTRA_PLACE_DETAILS, happyPlaceDetailModel)
        fragment.arguments = bundle

        val transaction =  manager.beginTransaction()
        transaction.replace(R.id.fragment_cntainer, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun replaceFragment(fragment: Fragment?) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_cntainer, fragment)
            transaction.commit()
        }
    }
}