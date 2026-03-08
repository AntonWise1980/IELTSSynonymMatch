package com.example.ieltssynonymmatch

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomnavigation.BottomNavigationView

class AnasayfaFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        val btnOyun1 = view.findViewById<Button>(R.id.btnStartOyun1)
        val btnOyun2 = view.findViewById<Button>(R.id.btnStartOyun2)

        btnOyun1.setOnClickListener {
            navigateToFragment(Oyun1Fragment(), R.id.nav_game)
        }

        btnOyun2.setOnClickListener {
            navigateToFragment(Oyun2Fragment(), R.id.nav_games_list)
        }

        return view
    }

    private fun navigateToFragment(fragment: Fragment, menuItemId: Int) {
        // Fragment'ı değiştir
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        // Bottom Navigation'daki seçili öğeyi de güncelle
        activity?.findViewById<BottomNavigationView>(R.id.bottom_navigation)?.selectedItemId = menuItemId
    }
}