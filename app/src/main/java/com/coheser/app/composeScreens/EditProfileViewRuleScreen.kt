package com.coheser.app.composeScreens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coheser.app.R
import com.coheser.app.simpleclasses.Variables
import com.coheser.app.viewModels.ProfileViewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileViewRuleScreen(viewModel: ProfileViewsViewModel) {

    var isProfileViewHistoryEnabled by remember { mutableStateOf(false) }
    isProfileViewHistoryEnabled=if(viewModel.isShowProfileHistory.value.equals("1")) true else false

    ModalBottomSheet( containerColor = colorResource(R.color.white),
    onDismissRequest = {viewModel.openSettingFragment.value=false }) {
        Box(
            modifier = Modifier.fillMaxSize()

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp,0.dp,16.dp,0.dp)
            ) {
                // Title Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = stringResource(id = R.string.profile_views),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorResource(R.color.black)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.openSettingFragment.value=false}) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(id = R.drawable.ic_cross),
                            contentDescription = "Close",
                            tint = colorResource(R.color.black)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.profile_view_history),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = isProfileViewHistoryEnabled,
                        onCheckedChange = {
                            isProfileViewHistoryEnabled = it
                            viewModel.isShowProfileHistory.value=if(it) "1" else "0"
                            viewModel.sharedPreferences.edit().putString(Variables.U_PROFILE_VIEW,viewModel.isShowProfileHistory.value).commit()
                            viewModel.updateProfileViewStatus()
                                          },
                        colors = SwitchDefaults.colors(checkedThumbColor = colorResource(R.color.appColor))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Description Text
                Text(
                    text = stringResource(id = R.string.profile_view_rule_description),
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
