package com.example.ieltssynonymmatch

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class StatsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("IELTS_STATS", Context.MODE_PRIVATE)

    var totalGamesPlayed: Int
        get() = prefs.getInt("total_games", 0)
        set(value) = prefs.edit().putInt("total_games", value).apply()

    var oyun1Count: Int
        get() = prefs.getInt("oyun1_count", 0)
        set(value) = prefs.edit().putInt("oyun1_count", value).apply()

    var oyun2Count: Int
        get() = prefs.getInt("oyun2_count", 0)
        set(value) = prefs.edit().putInt("oyun2_count", value).apply()

    var oyun3Count: Int
        get() = prefs.getInt("oyun3_count", 0)
        set(value) = prefs.edit().putInt("oyun3_count", value).apply()

    var oyun1HighScore: Int
        get() = prefs.getInt("oyun1_highscore", 0)
        set(value) = prefs.edit().putInt("oyun1_highscore", value).apply()

    var oyun2HighScore: Int
        get() = prefs.getInt("oyun2_highscore", 0)
        set(value) = prefs.edit().putInt("oyun2_highscore", value).apply()

    var oyun3HighScore: Int
        get() = prefs.getInt("oyun3_highscore", 0)
        set(value) = prefs.edit().putInt("oyun3_highscore", value).apply()

    var strikeDay: Int
        get() = prefs.getInt("strike_day", 0)
        set(value) = prefs.edit().putInt("strike_day", value).apply()

    var lastPlayDate: String?
        get() = prefs.getString("last_play_date", null)
        set(value) = prefs.edit().putString("last_play_date", value).apply()

    fun saveScoreOyun1(score: Int) {
        if (score > oyun1HighScore) oyun1HighScore = score
        addScoreToHistory("oyun1_history", score)
    }

    fun saveScoreOyun2(score: Int) {
        if (score > oyun2HighScore) oyun2HighScore = score
        addScoreToHistory("oyun2_history", score)
    }

    fun saveScoreOyun3(score: Int) {
        if (score > oyun3HighScore) oyun3HighScore = score
        addScoreToHistory("oyun3_history", score)
    }

    fun getOyun1History(): List<Int> = getIntList("oyun1_history")
    fun getOyun2History(): List<Int> = getIntList("oyun2_history")
    fun getOyun3History(): List<Int> = getIntList("oyun3_history")

    private fun addScoreToHistory(key: String, score: Int) {
        val list = getIntList(key)
        list.add(score)
        // Son 10 skoru tutalım ki grafik çok karmaşık olmasın
        val limitedList = if (list.size > 10) list.takeLast(10) else list
        saveIntList(key, limitedList)
    }

    // Soru Bazlı Takip (WordID_Synonym) - Oyun 1 için
    fun addLearnedQuestion(questionId: String) {
        val list = getStringList("learned_questions")
        if (!list.contains(questionId)) {
            list.add(questionId)
            saveStringList("learned_questions", list)
        }
    }

    fun getLearnedQuestionsCount(): Int = getStringList("learned_questions").size
    fun getLearnedQuestionIds(): List<String> = getStringList("learned_questions")

    // Kelime Bazlı Takip - Oyun 3 için
    fun addLearnedWord(wordId: Int) {
        val list = getIntList("learned_words")
        if (!list.contains(wordId)) {
            list.add(wordId)
            saveIntList("learned_words", list)
        }
        removeErrorWord(wordId)
    }

    fun getLearnedWordsCount(): Int = getIntList("learned_words").size
    fun getLearnedWordIds(): List<Int> = getIntList("learned_words")

    fun addErrorWord(wordId: Int) {
        val list = getIntList("error_words")
        if (!list.contains(wordId)) {
            list.add(wordId)
            saveIntList("error_words", list)
        }
    }

    private fun removeErrorWord(wordId: Int) {
        val list = getIntList("error_words")
        if (list.contains(wordId)) {
            list.remove(wordId)
            saveIntList("error_words", list)
        }
    }

    fun getErrorWordsCount(): Int = getIntList("error_words").size
    fun getErrorWordIds(): List<Int> = getIntList("error_words")

    fun updateStrike() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = sdf.format(Date())
        val lastDate = lastPlayDate

        if (lastDate == null) {
            strikeDay = 1
        } else if (lastDate != today) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = sdf.format(calendar.time)

            if (lastDate == yesterday) {
                strikeDay += 1
            } else {
                strikeDay = 1
            }
        }
        lastPlayDate = today
    }

    private fun getStringList(key: String): MutableList<String> {
        val json = prefs.getString(key, "[]")
        val array = JSONArray(json)
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    private fun saveStringList(key: String, list: List<String>) {
        val array = JSONArray()
        list.forEach { array.put(it) }
        prefs.edit().putString(key, array.toString()).apply()
    }

    private fun getIntList(key: String): MutableList<Int> {
        val json = prefs.getString(key, "[]")
        val array = JSONArray(json)
        val list = mutableListOf<Int>()
        for (i in 0 until array.length()) {
            list.add(array.getInt(i))
        }
        return list
    }

    private fun saveIntList(key: String, list: List<Int>) {
        val array = JSONArray()
        list.forEach { array.put(it) }
        prefs.edit().putString(key, array.toString()).apply()
    }
}