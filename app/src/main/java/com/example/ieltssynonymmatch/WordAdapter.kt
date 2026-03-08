package com.example.ieltssynonymmatch

import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WordAdapter(
    private var words: List<WordModel>,
    private val tts: TextToSpeech?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private var expandedPosition = -1

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)
    
    class WordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.cardWord)
        val wordTxt: TextView = view.findViewById(R.id.textViewWord)
        val meaningTxt: TextView = view.findViewById(R.id.textViewMeaning)
        val definitionTxt: TextView = view.findViewById(R.id.textViewDefinition)
        val exampleEnTxt: TextView = view.findViewById(R.id.textViewExampleEn)
        val exampleTrTxt: TextView = view.findViewById(R.id.textViewExampleTr)
        val synonymTxt: TextView = view.findViewById(R.id.textViewSynonyms)
        val btnSpeak: ImageButton = view.findViewById(R.id.btnSpeakItem)
        val layoutDetails: LinearLayout = view.findViewById(R.id.layoutDetails)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_header_info, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_word, parent, false)
            WordViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is WordViewHolder) {
            val currentWord = words[position - 1]
            holder.wordTxt.text = currentWord.word
            holder.meaningTxt.text = currentWord.meaning_tr
            holder.definitionTxt.text = currentWord.definition
            holder.exampleEnTxt.text = currentWord.example_sentence
            holder.exampleTrTxt.text = currentWord.meaning_example_sentence
            
            // Synonym listesini virgülle ayırarak yazdır
            holder.synonymTxt.text = currentWord.synonyms_list.joinToString(", ")

            val isExpanded = position == expandedPosition
            holder.layoutDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            holder.card.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyDataSetChanged()
            }

            holder.btnSpeak.setOnClickListener {
                tts?.speak(currentWord.word, TextToSpeech.QUEUE_FLUSH, null, "")
            }
        }
    }

    override fun getItemCount() = words.size + 1

    fun updateList(newList: List<WordModel>) {
        words = newList
        expandedPosition = -1
        notifyDataSetChanged()
    }
}