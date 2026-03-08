package com.example.ieltssynonymmatch

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.json.JSONArray
import java.util.Locale

class Oyun1Fragment : Fragment(R.layout.fragment_game), TextToSpeech.OnInitListener {

    private lateinit var allWords: List<WordModel>
    private lateinit var currentWord: WordModel
    private lateinit var currentCorrectSynonym: String
    private var score = 0
    private var lives = 5
    private var tts: TextToSpeech? = null
    private lateinit var statsManager: StatsManager

    private lateinit var tvLives: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvQuestionCount: TextView
    private lateinit var layoutIntro: LinearLayout
    private lateinit var layoutGameContent: LinearLayout

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statsManager = StatsManager(requireContext())
        tts = TextToSpeech(context, this)

        layoutIntro = view.findViewById(R.id.layoutIntro)
        layoutGameContent = view.findViewById(R.id.layoutGameContent)
        val btnPlayGame = view.findViewById<Button>(R.id.btnPlayGame)
        val btnEndGame = view.findViewById<Button>(R.id.btnEndGame)
        val btnSpeak = view.findViewById<ImageButton>(R.id.btnSpeak)
        tvLives = view.findViewById(R.id.textViewLives1)
        tvScore = view.findViewById(R.id.textViewScore)
        tvQuestionCount = view.findViewById(R.id.textViewQuestionCount)

        allWords = loadWordsFromAssets()

        btnPlayGame.setOnClickListener {
            startNewGame()
        }

        btnEndGame.setOnClickListener {
            gameOver()
        }

        btnSpeak.setOnClickListener {
            speakWord(currentWord.word)
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
        statsManager.oyun1Count++
        statsManager.updateStrike()
    }

    private fun updateUI() {
        tvScore.text = "Puan: $score"
        tvLives.text = "❤️".repeat(lives.coerceAtLeast(0))
        val learnedCount = statsManager.getLearnedQuestionsCount()
        val remainingCount = 620 - learnedCount
        tvQuestionCount.text = "Kalan Soru: $remainingCount"
    }

    private fun speakWord(word: String) {
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setLanguage(Locale.UK)
        }
    }

    private fun setupNewQuestion(view: View) {
        if (allWords.isEmpty()) return

        val learnedQuestionIds = statsManager.getLearnedQuestionIds()
        
        // 1. Filtreleme: Kelimenin en az bir bilinmeyen eşanlamlısı olmalı
        val availableWords = allWords.filter { word ->
            word.synonyms_list.any { syn -> !learnedQuestionIds.contains("${word.id}_$syn") }
        }

        if (availableWords.isEmpty()) {
            Toast.makeText(context, "Tebrikler! Tüm soruları tamamladınız.", Toast.LENGTH_LONG).show()
            gameOver()
            return
        }

        // Seçim mantığı: Hata yapılan ve henüz bilinmeyen kelimelerden %40 ihtimalle seç
        val errorWordIds = statsManager.getErrorWordIds()
        val errorAndAvailable = availableWords.filter { errorWordIds.contains(it.id) }

        currentWord = if (errorAndAvailable.isNotEmpty() && (0..100).random() < 40) {
            errorAndAvailable.random()
        } else {
            availableWords.random()
        }

        // 2. Doğru Cevap Seçimi (Bu kelimenin henüz bilinmeyen bir eşanlamlısı)
        val availableSynonyms = currentWord.synonyms_list.filter { syn ->
            !learnedQuestionIds.contains("${currentWord.id}_$syn")
        }
        currentCorrectSynonym = availableSynonyms.random()

        // 3. Yanlış Cevaplar
        val wrongAnswers = allWords
            .filter { it.id != currentWord.id }
            .flatMap { it.synonyms_list }
            .filter { it != currentCorrectSynonym }
            .shuffled()
            .take(3)

        val allOptions = (wrongAnswers + currentCorrectSynonym).shuffled()

        // 4. UI Bağlama
        val tvWord = view.findViewById<TextView>(R.id.textViewTargetWord)
        val tvMeaningTr = view.findViewById<TextView>(R.id.textViewMeaningTr)
        val tvExampleSentence = view.findViewById<TextView>(R.id.textViewExampleSentence)
        val buttons = listOf(
            view.findViewById<Button>(R.id.btnOption1),
            view.findViewById<Button>(R.id.btnOption2),
            view.findViewById<Button>(R.id.btnOption3),
            view.findViewById<Button>(R.id.btnOption4)
        )

        tvWord.text = currentWord.word
        tvMeaningTr.text = "(${currentWord.meaning_tr})"
        tvExampleSentence.text = "Example: ${currentWord.example_sentence}"

        buttons.forEachIndexed { index, button ->
            button.text = allOptions[index]
            button.isEnabled = true
            button.setBackgroundColor(Color.LTGRAY)
            
            button.setOnClickListener {
                buttons.forEach { it.isEnabled = false }

                if (button.text == currentCorrectSynonym) {
                    score += 10
                    button.setBackgroundColor(Color.GREEN)
                    statsManager.addLearnedQuestion("${currentWord.id}_$currentCorrectSynonym")
                    checkAndMarkWordAsLearned()
                    
                    updateUI()
                    view.postDelayed({ setupNewQuestion(view) }, 1000)
                } else {
                    lives--
                    button.setBackgroundColor(Color.RED)
                    statsManager.addErrorWord(currentWord.id)
                    buttons.find { it.text == currentCorrectSynonym }?.setBackgroundColor(Color.GREEN)
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
        statsManager.updateOyun1Score(score)
        Toast.makeText(context, "Oyun Bitti! Toplam Puan: $score", Toast.LENGTH_LONG).show()
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

    override fun onDestroyView() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroyView()
    }
}