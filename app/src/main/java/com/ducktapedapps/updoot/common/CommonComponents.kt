package com.ducktapedapps.updoot.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.data.local.model.Gildings
import com.ducktapedapps.updoot.theme.downVoteColor
import com.ducktapedapps.updoot.theme.upVoteColor
import com.ducktapedapps.updoot.utils.getCompactCountAsString

@Composable
fun VoteCounter(
    upVotes: Int?,
    userHasUpVoted: Boolean?,
    modifier: Modifier = Modifier,
    style:androidx.compose.ui.text.TextStyle,
) {
    Text(
        text = upVotes?.toLong()?.run { getCompactCountAsString(this) } ?: "?",
        style = style,
        color = when (userHasUpVoted) {
            true -> upVoteColor
            false -> downVoteColor
            null -> MaterialTheme.colorScheme.onBackground
        },
        modifier = modifier
    )
}

@Composable
fun AllGildings(gildings: Gildings, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (gildings.silverAwardCount != 0) {
            Text(
                text = gildings.silverAwardCount.toString(),
                style = MaterialTheme.typography.labelMedium
            )
            Image(
                painter = painterResource(id = R.drawable.ic_silver_gilding_14dp),
                contentDescription = stringResource(R.string.silver_award)
            )
        }

        if (gildings.goldAwardCount != 0) {
            Text(
                text = gildings.goldAwardCount.toString(),
                style = MaterialTheme.typography.labelSmall
            )
            Image(
                painter = painterResource(id = R.drawable.ic_gold_gilding_14dp),
                contentDescription = stringResource(R.string.gold_award)
            )

        }
        if (gildings.platinumAwardCount != 0) {
            Text(
                text = gildings.platinumAwardCount.toString(),
                style = MaterialTheme.typography.labelMedium
            )
            Image(
                painter = painterResource(id = R.drawable.ic_platinum_gilding_14dp),
                contentDescription = stringResource(R.string.platinum_award)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAllGildings() {
    AllGildings(gildings = Gildings(1, 2, 3), modifier = Modifier.wrapContentSize())
}

@Composable
fun PageLoadingFailed(performRetry: () -> Unit, message: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
        Button(onClick = performRetry) {
            Text(text = "Retry")
        }
        Text(text = message)
    }
}

@Composable
fun PageLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun PageEnd() {
    Text(text = "No more content", modifier = Modifier.padding(16.dp))
}