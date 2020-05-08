package com.ducktapedapps.updoot.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import java.util.*


/**
 * https://gist.github.com/JakeWharton/11274467
 *
 * A [SpannableStringBuilder] wrapper whose API doesn't make me want to stab my eyes out.
 * */
class Truss {
    private val builder: SpannableStringBuilder = SpannableStringBuilder()
    private val stack: Deque<Span> = ArrayDeque()

    fun append(string: String): Truss {
        builder.append(string)
        return this
    }

    fun append(charSequence: CharSequence): Truss {
        builder.append(charSequence)
        return this
    }

    fun append(c: Char): Truss {
        builder.append(c)
        return this
    }

    fun append(number: Int): Truss {
        builder.append(number.toString())
        return this
    }

    /** Starts `span` at the current position in the builder.  */
    fun pushSpan(span: Any?): Truss {
        stack.addLast(Span(builder.length, span))
        return this
    }

    /** End the most recently pushed span at the current position in the builder.  */
    fun popSpan(): Truss {
        val span = stack.removeLast()
        builder.setSpan(span.span, span.start, builder.length, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        return this
    }

    /** Create the final [CharSequence], popping any remaining spans.  */
    fun build(): CharSequence {
        while (!stack.isEmpty()) {
            popSpan()
        }
        return builder // TODO make immutable copy?
    }

    private class Span(val start: Int, val span: Any?)
}