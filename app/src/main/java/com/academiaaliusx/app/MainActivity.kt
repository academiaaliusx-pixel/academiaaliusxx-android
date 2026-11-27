package com.academiaaliusx.app

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Web-parity shell: render the official responsive AAX site in Android WebView.
 * Screen layout, navigation and page behavior are delegated to the official site
 * so the APK stays visually aligned with its mobile-web experience.
 */
class MainActivity : ComponentActivity() {
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var canGoBack by remember { mutableStateOf(false) }
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        webView = this
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = false
                        settings.setSupportZoom(false)
                        settings.builtInZoomControls = false
                        settings.displayZoomControls = false
                        settings.javaScriptCanOpenWindowsAutomatically = false
                        webChromeClient = WebChromeClient()
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String?) {
                                canGoBack = view.canGoBack()
                            }
                        }
                        loadUrl("https://academiaaliusx.com")
                    }
                },
                update = { canGoBack = it.canGoBack() }
            )
            BackHandler(enabled = canGoBack) {
                webView?.goBack()
                canGoBack = webView?.canGoBack() ?: false
            }
        }
    }

    override fun onDestroy() {
        webView?.apply {
            stopLoading()
            loadUrl("about:blank")
            clearHistory()
            destroy()
        }
        webView = null
        super.onDestroy()
    }
}
