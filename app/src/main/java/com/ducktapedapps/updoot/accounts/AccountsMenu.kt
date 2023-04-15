package com.ducktapedapps.updoot.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ducktapedapps.navigation.Event.ScreenNavigationEvent
import com.ducktapedapps.navigation.NavigationDirections.UserScreenNavigation
import com.ducktapedapps.updoot.MainActivityActions
import com.ducktapedapps.updoot.MainActivityActions.AddAccount
import com.ducktapedapps.updoot.MainActivityActions.RemoveAccount
import com.ducktapedapps.updoot.MainActivityActions.SendEvent
import com.ducktapedapps.updoot.MainActivityActions.SwitchToAccount
import com.ducktapedapps.updoot.R
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.AnonymousAccount
import com.ducktapedapps.updoot.utils.accountManagement.AccountModel.UserModel
import kotlinx.coroutines.launch

@Composable
fun AccountsBottomSheet(
    bottomSheetVisible: Boolean,
    hideBottomSheet: () -> Unit,
    accounts: List<AccountModel>,
    doAction: (MainActivityActions) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(key1 = bottomSheetVisible) {
        if (bottomSheetVisible) sheetState.show() else sheetState.hide()
    }
    if (bottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    hideBottomSheet()
                }
            },
            tonalElevation = 0.dp,
            sheetState = sheetState,
        ) {
            accounts.forEach { accountModel ->
                AccountMenuItem(
                    accountModel = accountModel,
                    openAccountInfo = {
                        doAction(
                            SendEvent(
                                ScreenNavigationEvent(
                                    UserScreenNavigation.open(it)
                                )
                            )
                        )
                    },
                    removeAccount = {
                        doAction(RemoveAccount(accountName = it))
                    },
                    switch = { doAction(SwitchToAccount(accountName = it)) }
                )
            }
            AddAccountItem {
                doAction(AddAccount)
            }


        }
    }

}

@Composable
private fun AddAccountItem(addAccount: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { addAccount() }
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = Icons.Default.AddCircle.name,
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = stringResource(id = R.string.add_account),
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
private fun AccountMenuItem(
    accountModel: AccountModel,
    openAccountInfo: (String) -> Unit,
    removeAccount: (accountName: String) -> Unit,
    switch: (accountName: String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                if (accountModel.isCurrent) openAccountInfo(accountModel.name)
                else switch(accountModel.name)
            }
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = if (accountModel.isCurrent) 0.2f else 0f),
                shape = RoundedCornerShape(50)
            )
            .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
    ) {
        when (accountModel) {
            is AnonymousAccount -> Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = Icons.Default.AccountCircle.name,
                modifier = Modifier.size(48.dp)
            )

            is UserModel -> AsyncImage(
                model = accountModel.userIcon,
                error = painterResource(id = R.drawable.ic_account_circle_24dp),
                contentDescription = Icons.Default.AccountCircle.name,
                modifier = Modifier
                    .clip(shape = CircleShape)
                    .size(48.dp)
            )
        }
        Text(
            text = accountModel.name, modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )

        when (accountModel) {
            is UserModel -> {
                IconButton(onClick = { openAccountInfo(accountModel.name) }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = Icons.Default.Info.name
                    )
                }
                IconButton(onClick = { removeAccount(accountModel.name) }) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = Icons.Default.ExitToApp.name
                    )
                }
            }

            is AnonymousAccount -> Unit
        }
    }
}