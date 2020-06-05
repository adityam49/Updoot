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
import com.ducktapedapps.updoot.ui.subreddit.SubmissionsVM
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
                                    .setSubreddit(linkData.subredditName)
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