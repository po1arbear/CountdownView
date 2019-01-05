package com.orangeaterz.countdownview

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.TextView
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class CountdownView : TextView {
    private var mListener: OnCountdownListener? = null
    private var mTimeOut: Long? = 20
    private var mDisposable: Disposable? = null
    private var mEndWords: String? = ""
    private var mEnabledBackground: Drawable? = null
    private var mDisabledBackground: Drawable? = null
    private var mEnabledTextColor: Int? = null
    private var mDisabledTextColor: Int? = null

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.CountdownView)
        mDisabledBackground = typedArray?.getDrawable(R.styleable.CountdownView_disableBackground)
        mEnabledBackground = typedArray?.getDrawable(R.styleable.CountdownView_enableBackground)
        mDisabledTextColor = typedArray?.getColor(R.styleable.CountdownView_disabledTextColor, 0)
        mEnabledTextColor = typedArray?.getColor(R.styleable.CountdownView_enabledTextColor, 0)
        typedArray?.recycle()
    }

    fun start(lifecycleProvider: MainActivity) {
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
                        enable()
                    } else {
                        disable()
                        text = (mTimeOut!! - t).toString()
                        mListener?.onProgress(mTimeOut!! - t)
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

    fun setOnCountdownListener(listener: OnCountdownListener): CountdownView {
        mListener = listener
        return this
    }

    fun disabledTextColor(color: Int): CountdownView {
        mDisabledTextColor = color
        return this
    }

    fun enabledTextColor(color: Int): CountdownView {
        mEnabledTextColor = color
        return this
    }

    fun enabledBackground(drawable: Drawable): CountdownView {
        this.mEnabledBackground = drawable
        return this
    }

    fun disabledBackground(drawable: Drawable) {
        this.mDisabledBackground = drawable
    }

    fun endWords(words: String): CountdownView {
        this.mEndWords = words
        return this
    }

    private fun disable() {
        setTextColor(mDisabledTextColor!!)
        background = mEnabledBackground
    }

    private fun enable() {
        text = mEndWords
        mDisposable?.dispose()
        setTextColor(mEnabledTextColor!!)
        background = mDisabledBackground
    }

}