package com.tehmou.book.androidnewsreaderexample.network

import android.util.Log
import io.reactivex.Observable
import io.reactivex.functions.Function
import okhttp3.Response

class FeedObservable {
    companion object {
        private val TAG = FeedObservable::class.java.simpleName


        fun getFeed(url: String): Observable<List<Entry>> {
            return RawNetworkObservable.create(url)
                    .map {
                        val parser = FeedParser()
                        try {
                            val entries: List<Entry?>? = parser.parse(it.body()!!.byteStream())
                            Log.v(TAG, "Number of entries from url " + url + ": " + entries!!.size)
                            return@map entries as List<Entry>?
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing feed", e)
                        }
                        return@map arrayListOf<Entry>()
                    }
        }
    }

}