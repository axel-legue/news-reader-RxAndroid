package com.tehmou.book.androidnewsreaderexample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tehmou.book.androidnewsreaderexample.network.Entry
import com.tehmou.book.androidnewsreaderexample.network.FeedObservable
import io.reactivex.android.schedulers.AndroidSchedulers

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val googleFeedObservable = FeedObservable.getFeed(GOOGLE_NEWS)
        val registerFeedObservable = FeedObservable.getFeed(REGISTER_NEWS)

        googleFeedObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::drawList)

        registerFeedObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::drawList)

    }

    private fun drawList(entries: List<Entry>) {
        for (i in entries) {
            Log.i("MainActivity", "$i \n")
        }
    }


}