package com.ulesson.chat.main.model

data class Question(
    val id: Long,
    val questionUrl: String,
    val questionText: String,
    val subjectName: String,
    val subjectIcon: Int,
    val date: String
)
