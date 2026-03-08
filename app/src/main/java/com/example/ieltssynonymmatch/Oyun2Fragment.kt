package com.example.ieltssynonymmatch

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONArray

class Oyun2Fragment : Fragment(R.layout.fragment_games_list) {

    private lateinit var allWords: List<WordModel>
    private var lives = 5
    private var score = 0
    private var matchesCount = 0
    private lateinit var statsManager: StatsManager

    private var selectedLeftButton: Button? = null
    private var selectedRightButton: Button? = null
    private var selectedLeftWord: String? = null
    private var selectedRightSynonym: String? = null

    private lateinit var leftButtons: List<Button>
    private lateinit var rightButtons: List<Button>
    private lateinit var tvLives: TextView
    private lateinit var tvScore: TextView
    private lateinit var layoutIntro: LinearLayout
    private lateinit var layoutGameContent: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statsManager = StatsManager(requireContext())
        allWords = loadWordsFromAssets()

        layoutIntro = view.findViewById(R.id.layoutIntroMatch)
        layoutGameContent = view.findViewById(R.id.layoutGameMatchContent)
        val btnPlay = view.findViewById<Button>(R.id.btnPlayMatch)
        val btnEndGame = view.findViewById<Button>(R.id.btnEndMatchGame)

        tvLives = view.findViewById(R.id.textViewLives)
        tvScore = view.findViewById(R.id.textViewMatchScore)

        leftButtons = listOf(
            view.findViewById(R.id.btnLeft1),
            view.findViewById(R.id.btnLeft2),
            view.findViewById(R.id.btnLeft3),
            view.findViewById(R.id.btnLeft4)
        )

        rightButtons = listOf(
            view.findViewById(R.id.btnRight1),
            view.findViewById(R.id.btnRight2),
            view.findViewById(R.id.btnRight3),
            view.findViewById(R.id.btnRight4)
        )

        btnPlay.setOnClickListener {
            lives = 5
            score = 0
            layoutIntro.visibility = View.GONE
            layoutGameContent.visibility = View.VISIBLE
            setupNewRound()
            
            statsManager.totalGamesPlayed++
            statsManager.oyun2Count++
            statsManager.updateStrike()
        }

        btnEndGame.setOnClickListener {
            gameOver()
        }
    }

    private fun setupNewRound() {
        if (allWords.size < 4) return

        matchesCount = 0
        val roundWords = allWords.shuffled().take(4)
        val leftOptions = roundWords.map { it.word }.shuffled()
        val rightOptions = roundWords.map { it.synonyms_list.random() }.shuffled()

        leftButtons.forEachIndexed { index, button ->
            button.text = leftOptions[index]
            button.isEnabled = true
            button.setBackgroundColor(Color.LTGRAY)
            button.setOnClickListener { handleLeftSelection(button) }
        }

        rightButtons.forEachIndexed { index, button ->
            button.text = rightOptions[index]
            button.isEnabled = true
            button.setBackgroundColor(Color.LTGRAY)
            button.setOnClickListener { handleRightSelection(button) }
        }

        updateUI()
    }

    private fun handleLeftSelection(button: Button) {
        selectedLeftButton?.setBackgroundColor(Color.LTGRAY)
        selectedLeftButton = button
        selectedLeftWord = button.text.toString()
        button.setBackgroundColor(Color.YELLOW)
        checkMatch()
    }

    private fun handleRightSelection(button: Button) {
        selectedRightButton?.setBackgroundColor(Color.LTGRAY)
        selectedRightButton = button
        selectedRightSynonym = button.text.toString()
        button.setBackgroundColor(Color.YELLOW)
        checkMatch()
    }

    private fun checkMatch() {
        if (selectedLeftWord != null && selectedRightSynonym != null) {
            val wordObj = allWords.find { it.word == selectedLeftWord }
            val isCorrect = wordObj?.synonyms_list?.contains(selectedRightSynonym) == true

            if (isCorrect) {
                selectedLeftButton?.isEnabled = false
                selectedRightButton?.isEnabled = false
                selectedLeftButton?.setBackgroundColor(Color.GREEN)
                selectedRightButton?.setBackgroundColor(Color.GREEN)
                score += 20
                matchesCount++
                
                wordObj?.let { statsManager.addLearnedWord(it.id) }

                if (matchesCount == 4) {
                    Toast.makeText(context, "Harika! Yeni tur başlıyor.", Toast.LENGTH_SHORT).show()
                    view?.postDelayed({ setupNewRound() }, 500)
                }
            } else {
                lives--
                Toast.makeText(context, "Yanlış eşleştirme!", Toast.LENGTH_SHORT).show()
                selectedLeftButton?.setBackgroundColor(Color.LTGRAY)
                selectedRightButton?.setBackgroundColor(Color.LTGRAY)
                
                wordObj?.let { statsManager.addErrorWord(it.id) }
                
                if (lives <= 0) {
                    gameOver()
                }
            }

            selectedLeftButton = null
            selectedRightButton = null
            selectedLeftWord = null
            selectedRightSynonym = null
            updateUI()
        }
    }

    private fun updateUI() {
        tvScore.text = "Score: $score"
        val hearts = "❤️".repeat(lives.coerceAtLeast(0))
        tvLives.text = hearts
    }

    private fun gameOver() {
        statsManager.saveScoreOyun2(score)
        Toast.makeText(context, "Oyun Bitti! Toplam Skor: $score", Toast.LENGTH_LONG).show()
        layoutGameContent.visibility = View.GONE
        layoutIntro.visibility = View.VISIBLE
    }

    private fun loadWordsFromAssets(): List<WordModel> {
        val wordList = mutableListOf<WordModel>()
        try {
            val jsonString = requireContext().assets.open("synonyms.json").bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val synonyms = mutableListOf<String>()
                val synArray = obj.getJSONArray("synonyms_list")
                for (j in 0 until synArray.length()) synonyms.add(synArray.getString(j))

                wordList.add(WordModel(
                    obj.getInt("id"), obj.getString("word"), obj.getString("definition"),
                    obj.getString("meaning_tr"), obj.getString("example_sentence"),
                    obj.getString("meaning_example_sentence"), synonyms, obj.getString("level")
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return wordList
    }
}