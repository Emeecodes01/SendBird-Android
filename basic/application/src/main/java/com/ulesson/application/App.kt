package com.ulesson.application

import com.sendbird.android.sample.main.BaseApplication

class App: BaseApplication() {

    override val apiToken: String
        get() = "2b6e9d6693e2e402ee84f5306206dc0b638bb6dc"

    override val sendBirdAppId: String
        get() = "74FBB89C-C9B6-4E15-99E6-1AF8E67CEFFC"


    override val baseUrl: String
        get() = "TODO("
}