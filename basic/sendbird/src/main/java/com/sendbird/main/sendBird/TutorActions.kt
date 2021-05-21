package com.sendbird.main.sendBird

import com.sendbird.android.Member

interface TutorActions {
    fun showTutorProfile(members: List<Member>)
    fun showTutorRating(questionMap : MutableMap<String, Any?>)
}


