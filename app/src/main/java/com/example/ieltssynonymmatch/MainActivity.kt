package com.example.ieltssynonymmatch
import com.example.ieltssynonymmatch.R
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Dinleyiciyi önce ata ki selectedItemId değişikliği tetiklensin
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_game -> replaceFragment(Oyun1Fragment())
                R.id.nav_games_list -> replaceFragment(Oyun2Fragment())
                R.id.nav_stats -> replaceFragment(AnasayfaFragment())
                R.id.nav_words -> replaceFragment(WordsFragment())
                R.id.nav_profile -> replaceFragment(ProfileFragment())
                else -> false
            }
            true
        }

        // Uygulama ilk açıldığında varsayılan olarak Anasayfa ikonunu seçili yap
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_stats
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}