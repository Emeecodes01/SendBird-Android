package com.ulesson.application

import com.sendbird.android.sample.main.BaseApplication

class App: BaseApplication() {

    override val apiToken: String
        get() = "cc091d5681b3313950c4a757477f03f15e01f0c1"

    override val sendBirdAppId: String
        get() = "AB81507A-0EBB-46DB-BAD1-9B4667716E6A"
}