package com.example.ieltssynonymmatch

data class WordModel(
    val id: Int,
    val word: String,
    val definition: String,
    val meaning_tr: String,
    val example_sentence: String,
    val meaning_example_sentence: String,
    val synonyms_list: List<String>,
    val level: String
)