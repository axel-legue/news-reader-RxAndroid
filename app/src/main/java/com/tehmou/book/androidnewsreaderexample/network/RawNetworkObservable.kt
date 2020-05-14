package com.tehmou.book.androidnewsreaderexample.network

import android.content.ContentValues.TAG
import android.util.Log
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException


class RawNetworkObservable {

    private val TAG: String? = RawNetworkObservable::class.java.simpleName

    companion object {
        fun create(url: String): Observable<Response> {
            return Observable.create(
                    object : ObservableOnSubscribe<Response> {
                        var client: OkHttpClient = OkHttpClient()

                        @Throws(Exception::class)
                        override fun subscribe(emitter: ObservableEmitter<Response>) {
                            try {
                                val response: Response = client.newCall(Request.Builder().url(url).build()).execute()
                                emitter.onNext(response)
                                emitter.onComplete()
                                if (!response.isSuccessful) emitter.onError(Exception("error"))
                            } catch (e: IOException) {
                                emitter.onError(e)
                            }
                        }
                    }).subscribeOn(Schedulers.io())
        }

        fun getString(url: String): Observable<String> {
            return create(url)
                    .map { response ->
                        try {
                            return@map response.body()?.string()
                        } catch (e: java.lang.Exception) {
                            Log.e(TAG, "Error reading url $url")
                        }
                        null
                    }
        }
    }


}