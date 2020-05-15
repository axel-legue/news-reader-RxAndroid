package com.tehmou.book.androidnewsreaderexample


import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.tehmou.book.androidnewsreaderexample.network.Entry
import com.tehmou.book.androidnewsreaderexample.network.FeedObservable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val googleFeedObservable = FeedObservable.getFeed(GOOGLE_NEWS)
        val registerFeedObservable = FeedObservable.getFeed(REGISTER_NEWS)
        val combineLatestObservable: Observable<List<Entry>> =
                Observable.combineLatest(
                        googleFeedObservable,
                        registerFeedObservable,
                        BiFunction { googleList, registerList -> mergeList(googleList, registerList) }
                )

//        googleFeedObservable
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::drawList)
//
//        registerFeedObservable
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(this::drawList)


        combineLatestObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::drawList)


    }

    private fun drawList(entries: List<Entry>) {
        val itemsAdapter: ArrayAdapter<Entry> = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                entries)
        feedsList.adapter = itemsAdapter
    }

    private fun mergeList(entriesA: List<Entry>, entriesB: List<Entry>): List<Entry> {
        val finalList = mutableListOf<Entry>()
        finalList.addAll(entriesA)
        finalList.addAll(entriesB)
        return finalList
    }


}