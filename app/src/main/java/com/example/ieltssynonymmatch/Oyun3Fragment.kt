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

class Oyun3Fragment : Fragment(R.layout.fragment_game3) {

    private lateinit var allWords: List<WordModel>
    private lateinit var currentWord: WordModel
    private lateinit var currentCorrectWord: String
    private var score = 0
    private var lives = 5
    private lateinit var statsManager: StatsManager

    private lateinit var tvLives: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvQuestionCount: TextView
    private lateinit var tvDefinition: TextView
    private lateinit var tvExample: TextView
    private lateinit var tvSynonyms: TextView
    private lateinit var layoutIntro: LinearLayout
    private lateinit var layoutGameContent: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statsManager = StatsManager(requireContext())
        allWords = loadWordsFromAssets()

        layoutIntro = view.findViewById(R.id.layoutIntro3)
        layoutGameContent = view.findViewById(R.id.layoutGame3Content)
        val btnPlay = view.findViewById<Button>(R.id.btnPlayGame3)
        val btnEndGame = view.findViewById<Button>(R.id.btnEndGame3)

        tvLives = view.findViewById(R.id.textViewLives3)
        tvScore = view.findViewById(R.id.textViewScore3)
        tvQuestionCount = view.findViewById(R.id.textViewQuestionCount3)
        tvDefinition = view.findViewById(R.id.textViewDefinition3)
        tvExample = view.findViewById(R.id.textViewExample3)
        tvSynonyms = view.findViewById(R.id.textViewSynonyms3)

        btnPlay.setOnClickListener {
            startNewGame()
        }

        btnEndGame.setOnClickListener {
            gameOver()
        }
    }

    private fun startNewGame() {
        score = 0
        lives = 5
        layoutIntro.visibility = View.GONE
        layoutGameContent.visibility = View.VISIBLE
        updateUI()
        setupNewQuestion(requireView())
        
        statsManager.totalGamesPlayed++
        statsManager.oyun3Count++
        statsManager.updateStrike()
    }

    private fun updateUI() {
        tvScore.text = "Puan: $score"
        tvLives.text = "❤️".repeat(lives.coerceAtLeast(0))
        val learnedCount = statsManager.getLearnedQuestionsCount()
        val remainingCount = 620 - learnedCount
        tvQuestionCount.text = "Kalan Soru: $remainingCount"
    }

    private fun setupNewQuestion(view: View) {
        if (allWords.isEmpty()) return

        val learnedQuestionIds = statsManager.getLearnedQuestionIds()
        
        val availableWords = allWords.filter { word ->
            word.synonyms_list.any { syn -> !learnedQuestionIds.contains("${word.id}_$syn") }
        }

        if (availableWords.isEmpty()) {
            Toast.makeText(context, "Tebrikler! Tüm soruları tamamladınız.", Toast.LENGTH_LONG).show()
            gameOver()
            return
        }

        val errorWordIds = statsManager.getErrorWordIds()
        val errorAndAvailable = availableWords.filter { errorWordIds.contains(it.id) }

        currentWord = if (errorAndAvailable.isNotEmpty() && (0..100).random() < 40) {
            errorAndAvailable.random()
        } else {
            availableWords.random()
        }

        val availableSynonyms = currentWord.synonyms_list.filter { syn ->
            !learnedQuestionIds.contains("${currentWord.id}_$syn")
        }
        val currentCorrectSynonym = availableSynonyms.random()
        currentCorrectWord = currentWord.word

        val wrongAnswers = allWords
            .filter { it.id != currentWord.id }
            .shuffled()
            .take(3)
            .map { it.word }

        val allOptions = (wrongAnswers + currentCorrectWord).shuffled()

        tvDefinition.text = currentWord.definition
        tvExample.text = "Example: ${currentWord.example_sentence}"
        tvSynonyms.text = "Synonym Hint: $currentCorrectSynonym"

        val buttons = listOf(
            view.findViewById<Button>(R.id.btnOption1_3),
            view.findViewById<Button>(R.id.btnOption2_3),
            view.findViewById<Button>(R.id.btnOption3_3),
            view.findViewById<Button>(R.id.btnOption4_3)
        )

        buttons.forEachIndexed { index, button ->
            button.text = allOptions[index]
            button.isEnabled = true
            button.setBackgroundColor(Color.LTGRAY)
            
            button.setOnClickListener {
                buttons.forEach { it.isEnabled = false }

                if (button.text == currentCorrectWord) {
                    score += 15
                    button.setBackgroundColor(Color.GREEN)
                    statsManager.addLearnedQuestion("${currentWord.id}_$currentCorrectSynonym")
                    checkAndMarkWordAsLearned()
                    
                    updateUI()
                    view.postDelayed({ setupNewQuestion(view) }, 1000)
                } else {
                    lives--
                    button.setBackgroundColor(Color.RED)
                    statsManager.addErrorWord(currentWord.id)
                    buttons.find { it.text == currentCorrectWord }?.setBackgroundColor(Color.GREEN)
                    updateUI()

                    if (lives <= 0) {
                        view.postDelayed({ gameOver() }, 1900)
                    } else {
                        view.postDelayed({ setupNewQuestion(view) }, 1900)
                    }
                }
            }
        }
    }

    private fun checkAndMarkWordAsLearned() {
        val learnedQuestionIds = statsManager.getLearnedQuestionIds()
        val allSynonymsLearned = currentWord.synonyms_list.all { syn ->
            learnedQuestionIds.contains("${currentWord.id}_$syn")
        }
        if (allSynonymsLearned) {
            statsManager.addLearnedWord(currentWord.id)
        }
    }

    private fun gameOver() {
        statsManager.saveScoreOyun3(score)
        Toast.makeText(context, "Oyun Bitti! Skor: $score", Toast.LENGTH_LONG).show()
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