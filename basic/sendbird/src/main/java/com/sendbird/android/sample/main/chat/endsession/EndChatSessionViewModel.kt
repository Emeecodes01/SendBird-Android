package com.sendbird.android.sample.main.chat.endsession

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EndChatSessionViewModel: ViewModel() {

    val backToTimeline: MutableLiveData<Boolean> = MutableLiveData()

}