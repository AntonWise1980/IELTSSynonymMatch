package com.example.ieltssynonymmatch

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.util.Locale
import kotlin.concurrent.thread

class WordsFragment : Fragment(), TextToSpeech.OnInitListener {

    private lateinit var adapter: WordAdapter
    private var allWords: List<WordModel> = mutableListOf()
    private var tts: TextToSpeech? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_words, container, false)

        tts = TextToSpeech(context, this)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewWords)
        val searchView = view.findViewById<SearchView>(R.id.searchViewWords)

        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Başlangıçta boş adapter ata
        adapter = WordAdapter(mutableListOf(), tts)
        recyclerView.adapter = adapter

        // Verileri arka planda yükle
        loadDataAsync()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterWords(newText)
                return true
            }
        })

        return view
    }

    private fun loadDataAsync() {
        thread {
            val words = loadWordsFromAssets().sortedBy { it.word.lowercase() }
            activity?.runOnUiThread {
                allWords = words
                adapter.updateList(allWords)
            }
        }
    }

    private fun filterWords(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allWords
        } else {
            allWords.filter {
                it.word.contains(query, ignoreCase = true) ||
                it.meaning_tr.contains(query, ignoreCase = true)
            }
        }
        adapter.updateList(filteredList)
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.setLanguage(Locale.UK)
        }
    }

    override fun onDestroyView() {
        if (tts != null) {
            tts?.stop()
            tts?.shutdown()
        }
        super.onDestroyView()
    }
}