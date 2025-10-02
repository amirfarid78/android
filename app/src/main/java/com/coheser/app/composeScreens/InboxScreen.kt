package com.coheser.app.composeScreens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.coheser.app.R
import com.coheser.app.models.InboxModel
import com.coheser.app.simpleclasses.DateOprations
import com.coheser.app.viewModels.InboxViewModel

@Composable
fun InboxScreen(
    context: Context,
    viewModel: InboxViewModel,
    onBackPressed: () -> Unit,
    onChatSelected: (InboxModel) -> Unit
) {
    val inboxList by viewModel.inboxList.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        TopAppBar(
            title = { Text(text = stringResource(id = R.string.messages),textAlign = TextAlign.Center,) },
            backgroundColor = colorResource(R.color.white),
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(Icons.Default.ArrowBack,
                        contentDescription = "Back", tint = colorResource(R.color.black))
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.filteredInboxList(searchQuery)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .padding(start = 10.dp, end = 10.dp)
                .background(Color.LightGray, RoundedCornerShape(30.dp)) // Apply background and shape
                .padding(horizontal = 16.dp, vertical = 10.dp), // Manual inner padding
            textStyle = TextStyle(fontSize = 14.sp, color = colorResource(R.color.black)),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.search),
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                    innerTextField() // The actual input field
                }
            }
        )


        if (inboxList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = stringResource(id = R.string.you_don_t_have_any_chats_yet), color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(inboxList) { item ->
                    InboxItem(context,item = item, onClick = {
                        onChatSelected(item)
                    })
                }
            }
        }
    }
}
    @Composable
    fun InboxItem(context: Context,item: InboxModel, onClick: () -> Unit) {
        val isNewMessage = item.status == "0"
        Box(modifier = Modifier.clickable { onClick() }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Image
                AsyncImage(
                    model = item.pic,
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.ic_user_icon),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(6.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                ) {
                    // Username
                    Text(
                        text = item.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.black)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Message Preview
                        Text(
                            text = if (isNewMessage) "1 new message" else item.msg,
                            fontSize = 12.sp,
                            fontWeight = if (isNewMessage) FontWeight.Bold else FontWeight.Normal,
                            color = if (isNewMessage) Color.Black else Color.DarkGray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        // Message Date
                        Text(
                            text = DateOprations.changeDateTodayYesterday(context, item.date),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (isNewMessage) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, shape = CircleShape)
                    )
                }
            }
        }
    }