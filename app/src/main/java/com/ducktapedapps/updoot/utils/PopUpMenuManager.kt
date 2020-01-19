package com.ducktapedapps.updoot.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.navigation.NavController
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.model.LinkData
import com.ducktapedapps.updoot.ui.subreddit.QASSubredditVM
import com.ducktapedapps.updoot.ui.subreddit.SubredditFragmentDirections

fun showMenuFor(
        currentSubreddit: String?,
        linkData: LinkData,
        context: Context,
        anchorView: View?,
        navController: NavController
) {
    val popupMenu = PopupMenu(context, anchorView, Gravity.END)

    popupMenu.menuInflater.inflate(R.menu.submission_menu, popupMenu.menu)
    popupMenu.menu.findItem(R.id.item_author).title = linkData.author
    popupMenu.menu.findItem(R.id.item_subreddit).title = linkData.subredditName

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("", Constants.BASE_URL + linkData.permalink)

    popupMenu.setOnMenuItemClickListener { item ->
        when (item.itemId) {
            R.id.item_author -> Toast.makeText(context, linkData.author, Toast.LENGTH_SHORT).show()

            R.id.item_subreddit -> {
                if (currentSubreddit != linkData.subredditName) {
                    navController.navigate(
                            SubredditFragmentDirections
                                    .actionGoToSubreddit()
                                    .setRSubreddit(linkData.subredditName)
                    )
                    Toast.makeText(context, linkData.subredditName, Toast.LENGTH_SHORT).show()
                } else Toast.makeText(context, "You are already in $currentSubreddit", Toast.LENGTH_SHORT).show()
            }

            R.id.item_link -> {
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(context, "Link copied !", Toast.LENGTH_SHORT).show()
            }

            //TODO implementation -> offline support
            R.id.item_save_offline -> Toast.makeText(context, "Not implemented yet", Toast.LENGTH_SHORT).show()
        }
        true
    }
    popupMenu.show()
}

fun showMenuFor(context: Context, anchorView: View, qasSubredditVM: QASSubredditVM) {
    val popupMenu = PopupMenu(context, anchorView)

    popupMenu.menuInflater.inflate(R.menu.sort_menu, popupMenu.menu)

    popupMenu.setOnMenuItemClickListener { item ->
        qasSubredditVM.changeSort(when (item.itemId) {
            R.id.hot_item -> Sorting.HOT
            R.id.top_item -> Sorting.TOP
            R.id.controversial_item -> Sorting.CONTROVERSIAL
            R.id.best_item -> Sorting.BEST
            R.id.new_item -> Sorting.NEW
            else -> Sorting.RISING
        })
        true
    }

    popupMenu.show()
}