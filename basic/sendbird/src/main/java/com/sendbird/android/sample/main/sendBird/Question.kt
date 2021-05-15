package com.sendbird.android.sample.main.sendBird

data class Question (
    val created_at: String,
    val grade: String,
    val id: Int,
    val learner_avatar: String,
    val learner_id: Int,
    val learner_name: String,
    val question_image_url: String,
    val status: String,
    val subject: String
)