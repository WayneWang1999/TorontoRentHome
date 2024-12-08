package com.example.torontorenthome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.torontorenthome.databinding.ActivityMainBinding
import com.example.torontorenthome.ui.AccountFragment
import com.example.torontorenthome.ui.FavoriteFragment
import com.example.torontorenthome.ui.ListFragment
import com.example.torontorenthome.ui.MapFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize fragments
        val mapFragment = MapFragment()
        val listFragment = ListFragment()
        val accountFragment = AccountFragment()
        val favoriteFragment= FavoriteFragment()

        // Set the default fragment
        setCurrentFragment(mapFragment)

        // Handle navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener  {
            when (it.itemId) {
                R.id.miMap -> setCurrentFragment(mapFragment)
                R.id.miList -> setCurrentFragment(listFragment)
                R.id.miFavorite->setCurrentFragment(favoriteFragment)
                R.id.miAccount -> setCurrentFragment(accountFragment)
            }
            true
        }
    }

    // Helper function to set the current fragment
    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flMainFrame, fragment)
            commit()
        }
}