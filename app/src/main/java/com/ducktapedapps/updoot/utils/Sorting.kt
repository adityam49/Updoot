package com.ducktapedapps.updoot.utils

enum class Sorting {
    RISING {
        override fun toString() = Constants.RISING
    },
    BEST {
        override fun toString() = Constants.BEST
    },
    NEW {
        override fun toString() = Constants.NEW
    },
    HOT {
        override fun toString() = Constants.HOT
    },
    CONTROVERSIAL {
        override fun toString() = Constants.CONTROVERSIAL
    },
    TOP {
        override fun toString() = Constants.TOP
    };
}
