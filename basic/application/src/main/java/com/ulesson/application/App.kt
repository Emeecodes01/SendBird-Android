package com.ulesson.application

import com.sendbird.android.sample.main.BaseApplication

class App: BaseApplication() {

    override val apiToken: String
        get() = "3bc5424e4b6927e9cdeb285db32a39e794e18c2d"

    override val sendBirdAppId: String
        get() = "A7F28C00-59CB-47EF-940A-C6CA53E6DC35"


    override val baseUrl: String
        get() = "TODO("
}