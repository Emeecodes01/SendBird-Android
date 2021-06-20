package com.ulesson.chat.main.sendBird

import com.ulesson.chat.main.model.Question

interface ChatActions {
    fun chatReceived()
    fun showDummyChat(question: Question)
    fun getPendingQuestions()
}

