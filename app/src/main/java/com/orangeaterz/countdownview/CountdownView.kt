package com.orangeaterz.countdownview

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.AttributeSet
import android.widget.TextView
import com.trello.rxlifecycle2.LifecycleProvider
import com.trello.rxlifecycle2.android.ActivityEvent
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
    private var mNumberColor: Int? = null
    private var mSuffixColor: Int? = null
    private var mSuffixText: String? = ""
    private var mStatus = STATUS_ENABLED

    companion object {
        const val STATUS_DISABLED = 0x001
        const val STATUS_ENABLED = 0x002
    }

    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val typedArray = context?.obtainStyledAttributes(attrs, R.styleable.CountdownView)
        mDisabledBackground = typedArray?.getDrawable(R.styleable.CountdownView_disableBackground)
        mEnabledBackground = typedArray?.getDrawable(R.styleable.CountdownView_enableBackground)
        mDisabledTextColor = typedArray?.getColor(R.styleable.CountdownView_disabledTextColor, 0)
        mEnabledTextColor = typedArray?.getColor(R.styleable.CountdownView_enabledTextColor, 0)
        mNumberColor = typedArray?.getColor(R.styleable.CountdownView_numberColor, 0)
        mSuffixColor = typedArray?.getColor(R.styleable.CountdownView_suffixColor, 0)
        mSuffixText = typedArray?.getString(R.styleable.CountdownView_suffixText)
        typedArray?.recycle()
        setOnClickListener {
            if (mStatus == STATUS_ENABLED && mListener != null) {
                mListener?.onClick()
            }
        }
    }

//    tv.setText(Html.fromHtml( "<font color=#FF504B>"+Str1+"</font> "+ "<font color=#696969>"+Str2+"</font>"));


    fun start(lifecycleProvider: LifecycleProvider<ActivityEvent>) {
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
                        val number = (mTimeOut!! - t).toString()

                        text =
                                Html.fromHtml("<font color=$mNumberColor>$number</font> <font color=$mSuffixColor>$mSuffixText</font>")
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
        fun onClick()
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
        background = mDisabledBackground
        mStatus = STATUS_DISABLED
    }

    private fun enable() {
        mStatus = STATUS_ENABLED
        text = mEndWords
        mDisposable?.dispose()
        setTextColor(mEnabledTextColor!!)
        background = mEnabledBackground
    }

}