package com.tehmou.book.androidnewsreaderexample.network

import android.text.format.Time
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.text.ParseException


class FeedParser {
    // Constants indicting XML element names that we're interested in
    private val TAG_ID = 1
    private val TAG_TITLE = 2
    private val TAG_UPDATED = 3
    private val TAG_LINK = 4

    // We don't use XML namespaces
    private val ns: String? = null

    /** Parse an Atom feed, returning a collection of Entry objects.
     *
     * @param in Atom feed, as a stream.
     * @return List of [com.example.android.basicsyncadapter.net.FeedParser.Entry] objects.
     * @throws XmlPullParserException on error parsing feed.
     * @throws IOException on I/O error.
     */
    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    fun parse(`in`: InputStream): List<Entry?>? {
        return try {
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(`in`, null)
            parser.nextTag()
            readFeed(parser)
        } finally {
            `in`.close()
        }
    }

    /**
     * Decode a feed attached to an XmlPullParser.
     *
     * @param parser Incoming XMl
     * @return List of [com.example.android.basicsyncadapter.net.FeedParser.Entry] objects.
     * @throws XmlPullParserException on error parsing feed.
     * @throws IOException on I/O error.
     */
    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readFeed(parser: XmlPullParser): List<Entry?>? {
        val entries: MutableList<Entry?> = ArrayList()

        // Search for <feed> tags. These wrap the beginning/end of an Atom document.
        //
        // Example:
        // <?xml version="1.0" encoding="utf-8"?>
        // <feed xmlns="http://www.w3.org/2005/Atom">
        // ...
        // </feed>
        parser.require(XmlPullParser.START_TAG, ns, "feed")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            // Starts by looking for the <entry> tag. This tag repeates inside of <feed> for each
            // article in the feed.
            //
            // Example:
            // <entry>
            //   <title>Article title</title>
            //   <link rel="alternate" type="text/html" href="http://example.com/article/1234"/>
            //   <link rel="edit" href="http://example.com/admin/article/1234"/>
            //   <id>urn:uuid:218AC159-7F68-4CC6-873F-22AE6017390D</id>
            //   <updated>2003-06-27T12:00:00Z</updated>
            //   <summary>Article summary goes here.</summary>
            //   <author>
            //     <name>Rick Deckard</name>
            //     <email>deckard@example.com</email>
            //   </author>
            // </entry>
            if (name == "entry") {
                entries.add(readEntry(parser))
            } else {
                skip(parser)
            }
        }
        return entries
    }

    /**
     * Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them
     * off to their respective "read" methods for processing. Otherwise, skips the tag.
     */
    @Throws(XmlPullParserException::class, IOException::class, ParseException::class)
    private fun readEntry(parser: XmlPullParser): Entry? {
        parser.require(XmlPullParser.START_TAG, ns, "entry")
        var id: String = "id"
        var title: String = "title"
        var link: String = "link"
        var updatedOn: Long = 0
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = parser.name
            if (name == "id") {
                // Example: <id>urn:uuid:218AC159-7F68-4CC6-873F-22AE6017390D</id>
                id = readTag(parser, TAG_ID)
            } else if (name == "title") {
                // Example: <title>Article title</title>
                title = readTag(parser, TAG_TITLE)
            } else if (name == "link") {
                // Example: <link rel="alternate" type="text/html" href="http://example.com/article/1234"/>
                //
                // Multiple link types can be included. readAlternateLink() will only return
                // non-null when reading an "alternate"-type link. Ignore other responses.
                val tempLink = readTag(parser, TAG_LINK)
                link = tempLink
            } else if (name == "updated") {
                // Example: <updated>2003-06-27T12:00:00Z</updated>
                val t = Time()
                t.parse3339(readTag(parser, TAG_UPDATED))
                updatedOn = t.toMillis(false)
            } else {
                skip(parser)
            }
        }
        return Entry(id, title, link, updatedOn)
    }

    /**
     * Process an incoming tag and read the selected value from it.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(parser: XmlPullParser, tagType: Int): String {
        val tag: String = "null"
        val endTag: String = "null"
        return when (tagType) {
            TAG_ID -> readBasicTag(parser, "id")
            TAG_TITLE -> readBasicTag(parser, "title")
            TAG_UPDATED -> readBasicTag(parser, "updated")
            TAG_LINK -> readAlternateLink(parser)
            else -> throw IllegalArgumentException("Unknown tag type: $tagType")
        }
    }

    /**
     * Reads the body of a basic XML tag, which is guaranteed not to contain any nested elements.
     *
     *
     * You probably want to call readTag().
     *
     * @param parser Current parser object
     * @param tag XML element tag name to parse
     * @return Body of the specified tag
     * @throws IOException
     * @throws XmlPullParserException
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readBasicTag(parser: XmlPullParser, tag: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tag)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tag)
        return result
    }

    /**
     * Processes link tags in the feed.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readAlternateLink(parser: XmlPullParser): String {
        var link: String = "null"
        parser.require(XmlPullParser.START_TAG, ns, "link")
        val tag = parser.name
        val relType = parser.getAttributeValue(null, "rel")
        if (relType != null && relType == "alternate") {
            link = parser.getAttributeValue(null, "href")
        }
        while (true) {
            if (parser.nextTag() == XmlPullParser.END_TAG) break
            // Intentionally break; consumes any remaining sub-tags.
        }
        return link
    }

    /**
     * For the tags title and summary, extracts their text values.
     */
    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result: String = "null"
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    /**
     * Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
     * if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
     * finds the matching END_TAG (as indicated by the value of "depth" being 0).
     */
    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        check(parser.eventType == XmlPullParser.START_TAG)
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

}