package com.example.ieltssynonymmatch

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var statsManager: StatsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        statsManager = StatsManager(requireContext())

        val tvStrike = view.findViewById<TextView>(R.id.tvStrikeDay)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalGames)
        val tvLearned = view.findViewById<TextView>(R.id.tvLearnedCount)
        val tvError = view.findViewById<TextView>(R.id.tvErrorCount)
        val tvOyun1 = view.findViewById<TextView>(R.id.tvOyun1Count)
        val tvOyun2 = view.findViewById<TextView>(R.id.tvOyun2Count)
        val tvOyun1Max = view.findViewById<TextView>(R.id.tvOyun1Max)
        val tvOyun2Max = view.findViewById<TextView>(R.id.tvOyun2Max)

        tvStrike.text = statsManager.strikeDay.toString()
        tvTotal.text = statsManager.totalGamesPlayed.toString()
        tvLearned.text = statsManager.getLearnedWordsCount().toString()
        tvError.text = statsManager.getErrorWordsCount().toString()
        tvOyun1.text = statsManager.oyun1Count.toString()
        tvOyun2.text = statsManager.oyun2Count.toString()
        tvOyun1Max.text = statsManager.oyun1HighScore.toString()
        tvOyun2Max.text = statsManager.oyun2HighScore.toString()

        return view
    }
}