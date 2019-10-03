package com.ducktapedapps.updoot.utils

import android.text.Html
import android.widget.TextView

object MarkdownUtils {
    fun decodeAndSet(htmlEncodedString: String, textView: TextView) {
        var decodedString: String = htmlEncodedString
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&apos;", "'")
                .replace("&amp;", "&")
                .replace("<li><p>", "<p>• ")
                .replace("</li>", "<br>")
                .replace("<li.*?>".toRegex(), "•")
                .replace("<p>", "<div>")
                .replace("</p>", "</div>")
        decodedString = decodedString.substring(0, decodedString.lastIndexOf("\n"))
        //TODO : change Html.fromHtml to HtmlCompat.fromHtml
        val sequence = trim(Html.fromHtml(noTrailingWhiteLines(decodedString)))
        textView.text = sequence
    }
}

private fun noTrailingWhiteLines(text: String): String {
    var decodedText = text
    while (decodedText[decodedText.length - 1] == '\n') {
        decodedText = decodedText.substring(0, decodedText.length - 1)
    }
    return decodedText
}

private fun trim(s: CharSequence): CharSequence {
    var start = 0
    var end = s.length
    while (start < end && Character.isWhitespace(s[start])) {
        start++
    }
    while (end > start && Character.isWhitespace(s[end - 1])) {
        end--
    }
    return s.subSequence(start, end)
}
