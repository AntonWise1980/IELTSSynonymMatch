package com.example.ieltssynonymmatch

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

class GameFragment : Fragment(R.layout.fragment_game), TextToSpeech.OnInitListener {

    private lateinit var allWords: List<WordModel>
    private lateinit var currentWord: WordModel
    private var score = 0
    private var tts: TextToSpeech? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TextToSpeech başlatma
        tts = TextToSpeech(context, this)

        val layoutIntro = view.findViewById<LinearLayout>(R.id.layoutIntro)
        val layoutGameContent = view.findViewById<LinearLayout>(R.id.layoutGameContent)
        val btnPlayGame = view.findViewById<Button>(R.id.btnPlayGame)
        val btnEndGame = view.findViewById<Button>(R.id.btnEndGame)
        val btnSpeak = view.findViewById<ImageButton>(R.id.btnSpeak)

        allWords = loadWordsFromAssets()

        btnPlayGame.setOnClickListener {
            score = 0
            layoutIntro.visibility = View.GONE
            layoutGameContent.visibility = View.VISIBLE
            setupNewQuestion(view)
        }

        btnEndGame.setOnClickListener {
            layoutGameContent.visibility = View.GONE
            layoutIntro.visibility = View.VISIBLE
            Toast.makeText(context, "Oyun Bitti! Toplam Puan: $score", Toast.LENGTH_LONG).show()
        }

        btnSpeak.setOnClickListener {
            speakWord(currentWord.word)
        }
    }

    private fun speakWord(word: String) {
        tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // British English (UK) ayarı
            val result = tts?.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(context, "British aksanı desteklenmiyor, varsayılan dile dönülüyor.", Toast.LENGTH_SHORT).show()
                tts?.setLanguage(Locale.US)
            }
        } else {
            Toast.makeText(context, "TTS başlatılamadı!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupNewQuestion(view: View) {
        if (allWords.isEmpty()) return

        currentWord = allWords.random()
        val correctAnswer = currentWord.synonyms_list.random()
        val wrongAnswers = allWords
            .filter { it.id != currentWord.id }
            .flatMap { it.synonyms_list }
            .shuffled()
            .take(3)

        val allOptions = (wrongAnswers + correctAnswer).shuffled()

        val tvWord = view.findViewById<TextView>(R.id.textViewTargetWord)
        val tvMeaningTr = view.findViewById<TextView>(R.id.textViewMeaningTr)
        val tvExampleSentence = view.findViewById<TextView>(R.id.textViewExampleSentence)
        val tvScore = view.findViewById<TextView>(R.id.textViewScore)
        val buttons = listOf(
            view.findViewById<Button>(R.id.btnOption1),
            view.findViewById<Button>(R.id.btnOption2),
            view.findViewById<Button>(R.id.btnOption3),
            view.findViewById<Button>(R.id.btnOption4)
        )

        tvWord.text = currentWord.word
        tvMeaningTr.text = "(${currentWord.meaning_tr})"
        tvExampleSentence.text = "Example: ${currentWord.example_sentence}"
        tvScore.text = "Puan: $score"

        buttons.forEachIndexed { index, button ->
            button.text = allOptions[index]
            button.setOnClickListener {
                if (button.text == correctAnswer) {
                    score += 10
                    Toast.makeText(context, "Doğru!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Yanlış! Doğru cevap: $correctAnswer", Toast.LENGTH_SHORT).show()
                }
                setupNewQuestion(view)
            }
        }
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
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroyView()
    }
}