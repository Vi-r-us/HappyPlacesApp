package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.happyplaces.R
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
    }

    private fun replaceFragment(fragment: Fragment?) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_cntainer, fragment)
            transaction.commit()
        }
    }
}