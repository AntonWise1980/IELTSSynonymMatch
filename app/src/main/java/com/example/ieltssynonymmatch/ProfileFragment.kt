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

        // UI Elemanlarını Bağla
        val tvStrike = view.findViewById<TextView>(R.id.tvStrikeDay)
        val tvTotal = view.findViewById<TextView>(R.id.tvTotalGames)
        val tvLearned = view.findViewById<TextView>(R.id.tvLearnedCount)
        val tvError = view.findViewById<TextView>(R.id.tvErrorCount)
        val tvOyun1Count = view.findViewById<TextView>(R.id.tvOyun1Count)
        val tvOyun2Count = view.findViewById<TextView>(R.id.tvOyun2Count)
        val tvOyun3Count = view.findViewById<TextView>(R.id.tvOyun3Count)
        val tvOyun1Max = view.findViewById<TextView>(R.id.tvOyun1Max)
        val tvOyun2Max = view.findViewById<TextView>(R.id.tvOyun2Max)
        val tvOyun3Max = view.findViewById<TextView>(R.id.tvOyun3Max)
        
        val tvCorrectQuestions = view.findViewById<TextView>(R.id.tvCorrectQuestions)
        val tvRemainingQuestions = view.findViewById<TextView>(R.id.tvRemainingQuestions)

        // Grafikleri Bağla
        val graphOyun1 = view.findViewById<ProgressGraphView>(R.id.graphOyun1)
        val graphOyun2 = view.findViewById<ProgressGraphView>(R.id.graphOyun2)
        val graphOyun3 = view.findViewById<ProgressGraphView>(R.id.graphOyun3)

        // Verileri Yükle
        tvStrike.text = statsManager.strikeDay.toString()
        tvTotal.text = statsManager.totalGamesPlayed.toString()
        tvLearned.text = statsManager.getLearnedWordsCount().toString()
        tvError.text = statsManager.getErrorWordsCount().toString()
        
        tvOyun1Count.text = statsManager.oyun1Count.toString()
        tvOyun2Count.text = statsManager.oyun2Count.toString()
        tvOyun3Count.text = statsManager.oyun3Count.toString()
        
        tvOyun1Max.text = statsManager.oyun1HighScore.toString()
        tvOyun2Max.text = statsManager.oyun2HighScore.toString()
        tvOyun3Max.text = statsManager.oyun3HighScore.toString()
        
        // Soru Bankası
        val correctCount = statsManager.getLearnedQuestionsCount()
        tvCorrectQuestions.text = correctCount.toString()
        tvRemainingQuestions.text = (620 - correctCount).toString()

        // Grafikleri Güncelle
        graphOyun1.setData(statsManager.getOyun1History())
        graphOyun2.setData(statsManager.getOyun2History())
        graphOyun3.setData(statsManager.getOyun3History())

        return view
    }
}