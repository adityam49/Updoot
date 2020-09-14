package com.ducktapedapps.updoot.utils

import com.ducktapedapps.updoot.data.local.model.BaseComment
import com.ducktapedapps.updoot.data.local.model.LinkData
import com.ducktapedapps.updoot.data.local.model.ListingThing
import com.ducktapedapps.updoot.data.local.model.Subreddit
import com.ducktapedapps.updoot.data.local.moshiAdapters.Thing

fun Thing?.mapToRepliesModel(): List<BaseComment> =
        (this?.data as? ListingThing)
                ?.children
                ?.map { someThing -> someThing.data }
                ?.filterIsInstance(BaseComment::class.java)
                ?.toList() ?: emptyList()


fun Thing.asSubmissionsPage(): Pair<List<LinkData>, String?> = Pair(
        (data as? ListingThing)?.run {
            children
                    .map { someThing -> someThing.data }
                    .filterIsInstance(LinkData::class.java)
                    .toList()
        } ?: emptyList(),
        (data as? ListingThing)?.after
)

fun List<Thing>.asCommentPage(): Pair<LinkData, List<BaseComment>> = Pair(
        (this[0].data as? ListingThing)?.children?.first()?.data as LinkData,
        (this[1].data as? ListingThing)
                ?.children
                ?.map { something -> something.data }
                ?.filterIsInstance(BaseComment::class.java)
                ?.toList() ?: emptyList()
)

fun Thing.asSubredditPage(): Pair<List<Subreddit>, String?> =
        Pair((data as? ListingThing)
                ?.children
                ?.map { something -> something.data }
                ?.filterIsInstance(Subreddit::class.java)
                ?.toList() ?: emptyList(),
                (data as? ListingThing)?.after
        )