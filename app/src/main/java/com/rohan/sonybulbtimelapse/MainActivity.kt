package com.rohan.sonybulbtimelapse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.rohan.sonybulbtimelapse.databinding.ActivityMainBinding
import com.rohan.sonybulbtimelapse.ui.about.AboutFragment
import com.rohan.sonybulbtimelapse.ui.home.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        changeFragment(HomeFragment())

        binding.bottomNavbar.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.menu_home -> {
                    println("Change to Home")
                    changeFragment(HomeFragment())
                    true
                }
                R.id.menu_about -> {
                    println("Change to About")
                    changeFragment(AboutFragment())
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_layout, fragment)
        fragmentTransaction.commit()
    }

}