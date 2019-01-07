package com.orangeaterz.countdownview

import android.os.Bundle
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        countdownView.endWords("重新获取")
            .setOnCountdownListener(object : CountdownView.OnCountdownListener {
                override fun onError(e: Throwable) {
                }

                override fun onEnd() {
                }

                override fun onProgress(progress: Long) {
                }

                override fun onStart() {
                }

                override fun onClick() {
                    countdownView.start(this@MainActivity)
                }
            })

    }
}
