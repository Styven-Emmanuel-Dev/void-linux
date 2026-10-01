package com.voidlinux.feature.tor

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.voidlinux.feature.tor.R

/**
 * Navigateur Tor intégré : charge les URL http(s) et .onion via SOCKS.
 */
class OnionBrowserActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var urlBar: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onion_browser)

        webView = findViewById(R.id.webView)
        urlBar = findViewById(R.id.urlBar)
        progressBar = findViewById(R.id.progressBar)
        backButton = findViewById(R.id.backButton)

        setupWebView()
        setupUrlBar()
        setupBackButton()

        webView.loadUrl("https://check.torproject.org")
    }

    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.userAgentString = TorWebViewClient.USER_AGENT
        settings.mediaPlaybackRequiresUserGesture = false

        webView.webViewClient = TorWebViewClient(socksPort = 9050)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
                progressBar.visibility =
                    if (newProgress < 100) android.view.View.VISIBLE
                    else android.view.View.GONE
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                supportActionBar?.title = title ?: "Void Tor"
            }
        }
    }

    private fun setupUrlBar() {
        urlBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                navigate(urlBar.text.toString())
                true
            } else false
        }
    }

    private fun setupBackButton() {
        backButton.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
            else finish()
        }
    }

    private fun navigate(input: String) {
        val url = when {
            input.startsWith("http://") || input.startsWith("https://") -> input
            input.endsWith(".onion") -> "http://$input"
            input.contains(".") -> "https://$input"
            else -> "https://duckduckgo.com/?q=$input"
        }
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}