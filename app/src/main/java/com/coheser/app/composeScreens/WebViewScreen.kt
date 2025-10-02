package com.coheser.app.composeScreens

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.coheser.app.R


@SuppressLint("SetJavaScriptEnabled")
@Composable
 fun WebViewScreen(
    url: String,
    title: String,
    showAcceptButton: Boolean,
    onBackPress: () -> Unit,
    onAcceptClick: () -> Unit,
    onUrlChange: (String) -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {


            // Toolbar
            TopAppBar(
                title = { Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPress) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back_btn),
                            contentDescription = "Go Back",
                            tint = colorResource(R.color.black)
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                backgroundColor = colorResource(R.color.white)
            )

            // WebView
            AndroidView( modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        settings.javaScriptEnabled = true
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView, progress: Int) {
                                if (progress >= 80) {
                                    isLoading = false
                                }
                            }
                        }
                        webViewClient = object :android.webkit.WebViewClient(){
                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                request?.url.toString().let {
                                    onUrlChange(it)
                                }
                                return super.shouldOverrideUrlLoading(view, request)
                            }
                        }
                        loadUrl(url)
                    }
                }
            )

        }

        if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.Center),
            color = Color.Gray,
            strokeWidth = 3.dp
        )
        }

        if(showAcceptButton){
            AcceptButton(onAcceptClick,Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun AcceptButton(onClick: () -> Unit,modifier: Modifier) {
        Card(
            shape = RoundedCornerShape(25.dp),
            backgroundColor = Color.Red,
            modifier = modifier
                .padding(bottom = 10.dp)
                .size(170.dp, 40.dp)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Accept and Continue",
                    fontSize = 15.sp,
                    color = colorResource(R.color.white),
                    fontWeight = FontWeight.Bold
                )
            }
        }

}
