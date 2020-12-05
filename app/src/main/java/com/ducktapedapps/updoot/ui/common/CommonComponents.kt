package com.ducktapedapps.updoot.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.loadVectorResource
import androidx.compose.ui.tooling.preview.Preview
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.Gildings
import com.ducktapedapps.updoot.ui.theme.downVoteColor
import com.ducktapedapps.updoot.ui.theme.upVoteColor
import com.ducktapedapps.updoot.utils.getCompactCountAsString

@Composable
fun VoteCounter(ups: Int?, likes: Boolean?, modifier: Modifier = Modifier) {
    Text(
            text = ups?.toLong()?.run { getCompactCountAsString(this) } ?: "?",
            style = MaterialTheme.typography.overline,
            color = when (likes) {
                true -> upVoteColor
                false -> downVoteColor
                null -> MaterialTheme.colors.onBackground
            },
            modifier = modifier
    )
}

@Composable
fun AllGildings(gildings: Gildings, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (gildings.silver != 0) {
            Text(text = gildings.silver.toString(), style = MaterialTheme.typography.overline)
            loadVectorResource(id = R.drawable.ic_silver_gilding_14dp).resource.resource?.let {
                Image(imageVector = it)
            }
        }
        if (gildings.gold != 0) {
            Text(text = gildings.gold.toString(), style = MaterialTheme.typography.overline)
            loadVectorResource(id = R.drawable.ic_gold_gilding_14dp).resource.resource?.let {
                Image(imageVector = it)
            }

        }
        if (gildings.platinum != 0) {
            Text(text = gildings.platinum.toString(), style = MaterialTheme.typography.overline)
            loadVectorResource(id = R.drawable.ic_platinum_gilding_14dp).resource.resource?.let {
                Image(imageVector = it)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAllGildings() {
    AllGildings(gildings = Gildings(1, 2, 3), modifier = Modifier.wrapContentSize())
}

