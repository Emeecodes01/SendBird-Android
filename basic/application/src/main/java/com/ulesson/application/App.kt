package com.ulesson.application

import com.sendbird.android.sample.main.BaseApplication

class App: BaseApplication() {

    override val apiToken: String
        get() = "d7e6ec94571a0866270703273be79dad19b2713e"

    override val sendBirdAppId: String
        get() = "A1D36AE4-1FFF-4472-B2DA-AA237598ECB1"
}