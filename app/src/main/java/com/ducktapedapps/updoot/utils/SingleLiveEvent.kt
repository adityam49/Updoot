package com.ducktapedapps.updoot.utils

//event wrapper used to consume events such as Toast messages only once, not using this will cause the Toast message to be displayed again on config change
class SingleLiveEvent<T>(private val content: T) {
    private var isHandled: Boolean = false

    val contentIfNotHandled: T?
        get() {
            return if (!isHandled) {
                isHandled = true
                content
            } else {
                null
            }
        }

    init {
        this.isHandled = false
    }

    fun peekContent(): T {
        return this.content
    }
}
