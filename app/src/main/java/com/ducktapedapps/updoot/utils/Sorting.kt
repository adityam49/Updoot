package com.ducktapedapps.updoot.utils

enum class Sorting {
    RISING {
        override fun toString() = "rising"
    },
    BEST {
        override fun toString() = "best"
    },
    NEW {
        override fun toString() = "new"
    },
    HOT {
        override fun toString() = "hot"
    },
    CONTROVERSIAL {
        override fun toString() = "controversial"
    },
    TOP {
        override fun toString() = "top"
    };
}
