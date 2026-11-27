package com.academiaaliusx.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep system navigation/status areas outside the web content.
        WindowCompat.setDecorFitsSystemWindows(window, true)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    OfficialWebExperience()
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun OfficialWebExperience() {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    BackHandler(enabled = canGoBack) { webView?.goBack() }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadsImagesAutomatically = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = false
                settings.setSupportZoom(false)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        canGoBack = view.canGoBack()
                    }
                }
                webChromeClient = WebChromeClient()
                setOnScrollChangeListener { _, _, _, _, _ -> }
                loadUrl("https://academiaaliusx.com/")
                webView = this
            }
        },
        update = { view -> webView = view; canGoBack = view.canGoBack() }
    )
}
