package com.coheser.app.composeScreens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.coheser.app.R
import com.coheser.app.apiclasses.ApiResponce
import com.coheser.app.models.UserModel
import com.coheser.app.simpleclasses.Functions.capitalizeEachWord
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.ProfileViewsViewModel

@Composable
fun ProfileViewHistoryScreen(viewModel: ProfileViewsViewModel,
                             onbackpress:()->Unit,
                             onProfileClick:(UserModel)->Unit,
                             onFollowClick:(UserModel)->Unit) {
    val listState by viewModel.listLiveData.observeAsState()
    val openSetting by viewModel.openSettingFragment.observeAsState()
    val isProfileShow by viewModel.isShowProfileHistory.observeAsState()

    Column(modifier = Modifier.fillMaxSize().background(colorResource(R.color.white))) {
        Toolbar(viewModel,onbackpress)

        if(isProfileShow.equals("1")) {
            when (listState) {
                is ApiResponce.Loading -> {
                    ShimmerPlaceholder()
                }

                is ApiResponce.Error -> {
                    NoDataLayout()
                }

                is ApiResponce.Success -> {
                    val userList =
                        (listState as ApiResponce.Success<ArrayList<UserModel>>).data ?: emptyList()
                    if (userList.isNotEmpty()) {
                        ProfileListView(userList, onProfileClick, onFollowClick)
                    } else {
                        NoDataLayout()
                    }
                }

                else -> {}
            }
        }
        else{
            ProfileViewHistoryONScreen(viewModel,onbackpress)
        }

        if(openSetting == true) {
            EditProfileViewRuleScreen(viewModel)
        }
    }
}

@Composable
fun Toolbar(viewModel: ProfileViewsViewModel,onbackpress: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(colorResource(R.color.white))
            .padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = { onbackpress()}) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back_btn),
                contentDescription = "Back",
                modifier = Modifier.size(24.dp),
                tint = colorResource(R.color.black)
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(text = stringResource(id=R.string.profile_views), style = MaterialTheme.typography.h6, color = colorResource(R.color.black))
        Spacer(modifier = Modifier.weight(1f))

        IconButton(onClick = { viewModel.openSettingFragment.value=(true) }) {
            Icon(
                painter = painterResource(id = R.drawable.btn_setting),
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp),
                tint = colorResource(R.color.black)
            )
        }
    }
}

@Composable
fun ShimmerPlaceholder() {
    LazyColumn{ items(10){
        ShimmerEffectBox()
        Spacer(modifier = Modifier.height(10.dp))
    } }
}


@Composable
fun ShimmerEffectBox() {
    // Animate the shimmer effect
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.Gray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "translateX"
    )

    val shimmerBrush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(12.dp)
            .background(colorResource(R.color.white)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image Placeholder
        Box(
            modifier = Modifier
                .size(55.dp)
                .clip(CircleShape)
                .background(shimmerBrush)
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Name and Username Placeholder
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush)
            )
        }
    }

}

@Composable
fun NoDataLayout() {
    Box(
        modifier = Modifier.fillMaxSize(),

    ) {
        Text(text = stringResource(R.string.profile_view_description),
            color = colorResource(R.color.darkgray),
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.Center).padding(20.dp))

        Column (modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.whoops), color = Color.Gray, style = MaterialTheme.typography.h6)
            Text(
                text = stringResource(R.string.no_visitors),
                color = Color.Gray,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
fun ProfileListView(list:List<UserModel>,onProfileClick: (UserModel) -> Unit,onFollowClick: (UserModel) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(list) { item ->
            UserItem(item,onProfileClick,onFollowClick)

        }
    }
}

@Composable
fun UserItem(userModel: UserModel,
             onProfileClick: (UserModel) -> Unit,
             onFollowClick: (UserModel) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen._70sdp))
            .padding(
                start = dimensionResource(id = R.dimen._12sdp),
                end = dimensionResource(id = R.dimen._12sdp),
                top = dimensionResource(id = R.dimen._6sdp),
                bottom = dimensionResource(id = R.dimen._6sdp)
            ).clickable { onProfileClick(userModel) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image (Using Coil)
        AsyncImage(
            model = userModel.getProfilePic(),
            contentDescription = "User Profile",
            modifier = Modifier
                .size(dimensionResource(id = R.dimen._55sdp))
                .clip(CircleShape)
                .background(Color.LightGray),
            error = painterResource(id = R.drawable.ic_user_icon),
            placeholder = painterResource(id = R.drawable.ic_user_icon),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen._6sdp)))

        // User Info Column
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = dimensionResource(id = R.dimen._6sdp))
        ) {
            Text(
                text = userModel?.username!!,
                color = colorResource(R.color.black),
                fontSize = dimensionResource(id = R.dimen._11sdp).value.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._4sdp)))

            Text(
                text = userModel.first_name + " " + userModel.last_name,
                color = colorResource(R.color.black),
                fontSize = dimensionResource(id = R.dimen._10sdp).value.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }


        val (buttonBg, buttonTextColor) = when {
            userModel.button.equals("follow", ignoreCase = true) ||
                    userModel.button.equals("follow back", ignoreCase = true) -> {
                Color.Red to colorResource(R.color.whiteColor)
            }

            userModel.button.equals("following", ignoreCase = true) ||
                    userModel.button.equals("friends", ignoreCase = true) -> {
                Color.LightGray to Color.Black
            }
            else -> {
                Color.Gray to colorResource(R.color.white)
            }
        }
        Text(
            text = capitalizeEachWord(userModel.button!!),
            modifier = Modifier
                .width(dimensionResource(id = R.dimen._100sdp))
                .background(
                    color = buttonBg, // Replace with actual drawable background
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = dimensionResource(id = R.dimen._6sdp))
                .clickable { onFollowClick(userModel) },
            textAlign = TextAlign.Center,
            color = buttonTextColor,
            fontSize = dimensionResource(id = R.dimen._11sdp).value.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun ProfileViewHistoryONScreen(viewModel: ProfileViewsViewModel,onbackpress: () -> Unit) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.white))
            .padding(horizontal = dimensionResource(id = R.dimen._22sdp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._20sdp)))

        Image(
            painter = painterResource(id = R.drawable.ic_feature_view_history),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(id = R.dimen._200sdp))
        )

        Text(
            text = stringResource(id = R.string.turn_on_profile_view_history_),
            fontSize = dimensionResource(id = R.dimen._15sdp).value.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.black),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = dimensionResource(id = R.dimen._6sdp))
        )

        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen._20sdp)))

        val historyItems = listOf(
            R.drawable.ic_view_history_one to R.string.view_history_one,
            R.drawable.ic_view_history_two to R.string.view_history_two,
            R.drawable.ic_view_history_three to R.string.view_history_three,
            R.drawable.ic_view_history_four to R.string.view_history_four
        )

        historyItems.forEach { (icon, text) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = dimensionResource(id = R.dimen._10sdp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(dimensionResource(id = R.dimen._18sdp)),
                    colorFilter = ColorFilter.tint(colorResource(R.color.black))
                )

                Text(
                    text = stringResource(id = text),
                    fontSize = dimensionResource(id = R.dimen._11sdp).value.sp,
                    color = colorResource(R.color.black),
                    modifier = Modifier.padding(start = dimensionResource(id = R.dimen._6sdp))
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = dimensionResource(id = R.dimen._15sdp)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen._4sdp))
        ) {
            Button(
                onClick = {
                    viewModel.sharedPreferences.edit().putString(Variables.U_PROFILE_VIEW,"0").commit()
                    onbackpress()
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.Gainsboro)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.not_now),
                    fontSize = dimensionResource(id = R.dimen._11sdp).value.sp,
                    color = colorResource(R.color.black),
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    viewModel.sharedPreferences.edit().putString(Variables.U_PROFILE_VIEW,"1").commit()
                    viewModel.isShowProfileHistory.value="1"
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(R.color.appColor)),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.turn_on),
                    fontSize = dimensionResource(id = R.dimen._11sdp).value.sp,
                    color = colorResource(R.color.whiteColor),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
