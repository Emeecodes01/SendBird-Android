package com.ulesson.chat.main.sendBird

import com.sendbird.android.Member
import com.ulesson.chat.main.model.Question

interface TutorActions {
    fun showTutorProfile(members: List<Member>)
    fun showTutorRating(questionMap: MutableMap<String, Any?>)
}


