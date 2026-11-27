package com.academiaaliusx.app

import android.annotation.SuppressLint
import android.graphics.Color
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
        // Use a non-edge-to-edge WebView so fixed web navigation does not sit
        // underneath Android's system navigation controls.
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.statusBarColor = Color.rgb(5, 22, 29)
        window.navigationBarColor = Color.rgb(5, 22, 29)
        window.setNavigationBarContrastEnforced(true)
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
                settings.builtInZoomControls = false
                settings.displayZoomControls = false
                settings.mediaPlaybackRequiresUserGesture = true
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        canGoBack = view.canGoBack()
                        // Add breathing room for fixed/sticky site navigation and
                        // account for Android's gesture/three-button navigation.
                        view.evaluateJavascript("""
                            (function() {
                              var style = document.getElementById('aax-android-safe-area');
                              if (!style) {
                                style = document.createElement('style');
                                style.id = 'aax-android-safe-area';
                                document.head.appendChild(style);
                              }
                              style.textContent = 'html { scroll-padding-bottom: 100px !important; } body { padding-bottom: max(100px, env(safe-area-inset-bottom, 0px)) !important; }';
                              var candidates = document.querySelectorAll('nav, [role="navigation"], footer, [class*="bottom-nav"], [class*="bottomNav"], [class*="tab-bar"], [class*="tabBar"]');
                              candidates.forEach(function(el) {
                                var r = el.getBoundingClientRect();
                                var s = window.getComputedStyle(el);
                                if ((s.position === 'fixed' || s.position === 'sticky') && r.bottom > window.innerHeight - 180) {
                                  el.style.bottom = '8px';
                                  el.style.marginBottom = 'env(safe-area-inset-bottom, 0px)';
                                }
                              });
                            })();
                        """, null)
                    }
                }
                webChromeClient = WebChromeClient()
                loadUrl("https://academiaaliusx.com/")
                webView = this
            }
        },
        update = { view -> webView = view; canGoBack = view.canGoBack() }
    )
}
