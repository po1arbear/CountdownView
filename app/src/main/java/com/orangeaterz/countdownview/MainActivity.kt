package com.orangeaterz.countdownview

import android.os.Bundle
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        countdownView.bind(this)
        countdownView
            .setOnCountdownListener(object : CountdownView.AbsCountdownListener() {
                override fun onClick() {
                    super.onClick()
                    countdownView.start()
                }
            })

    }
}
