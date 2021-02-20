package com.ducktapedapps.updoot.utils.accountManagement

import com.ducktapedapps.updoot.data.remote.RedditAPI

interface RedditClient {
    /**
     *  All api calls go should get api service object via this method
     */
    suspend fun api(): RedditAPI
}