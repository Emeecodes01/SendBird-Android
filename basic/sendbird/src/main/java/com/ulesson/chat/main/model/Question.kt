package com.ulesson.chat.main.model

data class Question(
    val id: Long,
    val questionUrl: String? = "",
    val questionText: String? = "",
    val subjectName: String? = "",
    val subjectIcon: Int? = 0,
    val subjectThemeKey : String = "",
    val date: String
)
