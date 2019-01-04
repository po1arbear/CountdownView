package com.orangeaterz.countdownview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CountdownView : TextView {
    private var mListener: OnCountdownListener? = null
    private var mTimeOut: Long? = 60
    private var mDisposable: Disposable? = null
    private var mEndWords: String? = ""
    private var mEnabledBackground: Drawable? = null
    private var mDisabledBackground: Drawable? = null
    private var mEnabledTextColor: Int? = null
    private var mDisabledTextColor: Int? = null

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        //TODO
    }

    fun start(lifecycleProvider: LifecycleProvider<Any>) {
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(lifecycleProvider)
            .subscribe(object : Observer<Long> {
                override fun onComplete() {
                }

                override fun onSubscribe(d: Disposable) {
                    mDisposable = d
                    mListener?.onStart()
                }

                override fun onNext(t: Long) {
                    if (mTimeOut == t) {
                        mListener?.onEnd()
                        disable()
                    } else {
                        enable()
                        mListener?.onProgress(t)
                    }
                }

                override fun onError(e: Throwable) {
                    mListener?.onError(e)
                }

            })
    }


    interface OnCountdownListener {
        fun onStart()
        fun onProgress(progress: Long)
        fun onEnd()
        fun onError(e: Throwable)
    }

    fun setOnCountdownListener(listener: OnCountdownListener) {
        mListener = listener
    }

    fun disabledTextColor(color: Int): TextView {
        mDisabledTextColor = color
        return this
    }

    fun enabledTextColor(color: Int): TextView {
        mEnabledTextColor = color
        return this
    }

    fun enabledBackground(drawable: Drawable): TextView {
        this.mEnabledBackground = drawable
        return this
    }

    fun disabledBackground(drawable: Drawable) {
        this.mDisabledBackground = drawable
    }

    fun endWords(words: String): TextView {
        this.mEndWords = words
        return this
    }

    private fun disable() {
        mDisposable?.dispose()
        text = mEndWords
        setTextColor(mEnabledTextColor!!)
        background = mEnabledBackground
    }

    private fun enable() {
        text = mTimeOut.toString()
        setTextColor(mDisabledTextColor!!)
        background = mDisabledBackground
    }

}