package com.tehmou.book.androidnewsreaderexample.network

import java.util.*

class Entry(
        private val id: String,
        private val title: String,
        private val link: String,
        private val updated: Long) : Comparable<Entry> {


    override fun toString(): String {
        return Date(updated).toString() + "\n" + title
    }

    override fun compareTo(other: Entry): Int {
        if (updated > other.updated) {
            return -1
        } else if (updated < other.updated) {
            return 1
        }
        return 0
    }


}