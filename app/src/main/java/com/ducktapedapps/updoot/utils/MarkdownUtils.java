package com.ducktapedapps.updoot.utils;

import android.text.Html;
import android.widget.TextView;

public class MarkdownUtils {
    public static void decodeAndSet(String htmlEncodedString, TextView textView) {
        if (htmlEncodedString != null) {
            htmlEncodedString = htmlEncodedString
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&quot;", "\"")
                    .replace("&apos;", "'")
                    .replace("&amp;", "&")
                    .replace("<li><p>", "<p>• ")
                    .replace("</li>", "<br>")
                    .replaceAll("<li.*?>", "•")
                    .replace("<p>", "<div>")
                    .replace("</p>", "</div>");
            htmlEncodedString = htmlEncodedString.substring(0, htmlEncodedString.lastIndexOf("\n"));
            CharSequence sequence = trim(Html.fromHtml(noTrailingWhiteLines(htmlEncodedString)));
            textView.setText(sequence);
        }
    }

    private static String noTrailingWhiteLines(String text) {
        while (text.charAt(text.length() - 1) == '\n') {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static CharSequence trim(CharSequence s) {
        int start = 0;
        int end = s.length();
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }
        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }
        return s.subSequence(start, end);
    }
}
