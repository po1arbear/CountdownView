package com.orangeaterz.countdownview

import android.annotation.SuppressLint
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
    private var mEnabledBackground: Drawable? = null
    private var mDisabledBackground: Drawable? = null
    private var mEnabledTextColor: Int? = null
    private var mDisabledTextColor: Int? = null
    private var mNumberColor: Int? = null
    private var mSuffixColor: Int? = null
    private var mSuffixText: String? = ""
    private var mStartWords: String? = context.getString(R.string.send_code)
    private var mEndWords: String? = context.getString(R.string.resend)
    private var mStatus = STATUS_ENABLED
    private var mLifecycleProvider: LifecycleProvider<ActivityEvent>? = null

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
        mSuffixText = typedArray?.getString(R.styleable.CountdownView_suffixText) ?: context?.getString(R.string.resend)
        mStartWords = typedArray?.getString(R.styleable.CountdownView_startWords) ?:
                context?.getString(R.string.send_code)
        mEndWords = typedArray?.getString(R.styleable.CountdownView_endWords) ?: context?.getString(R.string.resend)
        mTimeOut = typedArray?.getInt(R.styleable.CountdownView_timeOut, 20)?.toLong()
        typedArray?.recycle()
        setOnClickListener {
            if (mStatus == STATUS_ENABLED && mListener != null) {
                mListener?.onClick()
            }
        }
    }

    fun bind(lifecycleProvider: LifecycleProvider<ActivityEvent>): CountdownView {
        this.mLifecycleProvider = lifecycleProvider
        return this
    }

    fun start() {
        if (mLifecycleProvider == null) {
            throw NullPointerException("please bind lifecycleProvider first")
        }
        Observable.interval(0, 1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .bindToLifecycle(mLifecycleProvider!!)
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
                        modePirvateCountdown(number)
                        mListener?.onProgress(mTimeOut!! - t)
                    }
                }

                override fun onError(e: Throwable) {
                    mListener?.onError(e)
                }

            })
    }

    fun stop() {
        if (mDisposable != null) {
            mDisposable?.dispose()
        }
    }

    fun reset() {
        stop()
        text = mStartWords
        enable()
    }

    fun restart() {
        reset()
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
        setTextColor(mDisabledTextColor!!)
    }

    private fun enable() {
        mStatus = STATUS_ENABLED
        text = mEndWords
        mDisposable?.dispose()
        setTextColor(mEnabledTextColor!!)
        background = mEnabledBackground
    }

    @SuppressLint("SetTextI18n")
    fun modePirvateCountdown(number: String) {

        text = "$mSuffixText($number" + "s)"
    }

    fun modeNormalCountdown(number: Long) {
        text =
                Html.fromHtml("<font color=$mNumberColor>$number</font> <font color=$mSuffixColor>$mSuffixText</font>")
    }

}