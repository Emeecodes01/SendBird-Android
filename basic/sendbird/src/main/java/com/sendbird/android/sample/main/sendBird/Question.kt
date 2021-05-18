package com.sendbird.android.sample.main.sendBird

data class Question (
    val accepted_at: String,
    val chat_session: String,
    val created_at: String,
    val grade: String,
    val id: Int,
    val learner_avatar: String,
    val learner_id: Int,
    val learner_name: String,
    val question_image_url: String,
    val question_text: String,
    val status: String,
    val subject: String,
    val tutor: Int,
    val tutor_id: Int,
    val startTime: String = "",
    val state: String // active or past
)