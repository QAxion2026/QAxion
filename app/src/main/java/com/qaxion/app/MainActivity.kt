package com.qaxion.app

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.os.Message
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qaxion.app.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var uploadCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null
    private var currentUrl: String = ""
    private var obraLabel: String = ""

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val results = when {
                data?.clipData != null -> Array(data.clipData!!.itemCount) { i ->
                    data.clipData!!.getItemAt(i).uri
                }
                data?.data != null -> arrayOf(data.data!!)
                cameraImageUri != null -> arrayOf(cameraImageUri!!)
                else -> null
            }
            uploadCallback?.onReceiveValue(results)
        } else {
            uploadCallback?.onReceiveValue(null)
        }
        uploadCallback = null
        cameraImageUri = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUrl = intent.getStringExtra("url") ?: "https://qaxion.com.br"
        obraLabel = intent.getStringExtra("label") ?: "QAxion"

        setupToolbar()
        setupWebView()
        setupSwipeRefresh()
        setupButtons()

        loadUrl(currentUrl)
    }

    private fun setupToolbar() {
        binding.tvTitle.text = obraLabel
        binding.btnBack.setOnClickListener {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                finish()
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            }
        }
        binding.btnMenu.setOnClickListener { showOptionsMenu() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        binding.webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                allowFileAccess = true
                allowContentAccess = true
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                builtInZoomControls = false
                displayZoomControls = false
                cacheMode = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
                userAgentString = "QAxion-Android/1.0 " + userAgentString
            }

            webViewClient = QAxionWebViewClient()
            webChromeClient = QAxionWebChromeClient()
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.apply {
            setColorSchemeResources(R.color.primary, R.color.accent)
            setOnRefreshListener {
                binding.webView.reload()
            }
        }
    }

    private fun setupButtons() {
        binding.btnRetry.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            loadUrl(currentUrl)
        }
    }

    private fun loadUrl(url: String) {
        currentUrl = url
        binding.webView.loadUrl(url)
    }

    private fun showOptionsMenu() {
        val options = arrayOf(
            "🔄  Recarregar página",
            "🏠  Ir para início da obra",
            "📋  Copiar URL",
            "🌐  Abrir no navegador",
            "🏢  Trocar de obra"
        )
        MaterialAlertDialogBuilder(this, R.style.QAxionDialog)
            .setTitle(obraLabel)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> binding.webView.reload()
                    1 -> loadUrl(currentUrl)
                    2 -> copyToClipboard(binding.webView.url ?: currentUrl)
                    3 -> openInBrowser(binding.webView.url ?: currentUrl)
                    4 -> { finish(); overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) }
                }
            }
            .show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", text))
        Toast.makeText(this, "URL copiada!", Toast.LENGTH_SHORT).show()
    }

    private fun openInBrowser(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    // ─── WebViewClient ────────────────────────────────────────
    inner class QAxionWebViewClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.progressBar.visibility = View.VISIBLE
            binding.swipeRefresh.isRefreshing = false
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            binding.progressBar.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
            binding.layoutError.visibility = View.GONE

            // Inject QAxion mobile enhancements
            injectMobileCSS(view)
        }

        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: WebResourceError?
        ) {
            if (request?.isForMainFrame == true) {
                binding.progressBar.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
                binding.tvErrorMessage.text = "Sem conexão ou servidor indisponível.\nVerifique sua internet e tente novamente."
            }
        }

        @SuppressLint("WebViewClientOnReceivedSslError")
        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            // For internal corporate systems - proceed with SSL warnings
            handler?.proceed()
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            val url = request?.url?.toString() ?: return false
            return when {
                url.startsWith("tel:") -> {
                    startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    true
                }
                url.startsWith("mailto:") -> {
                    startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse(url)))
                    true
                }
                url.contains("qaxion.com.br") -> false // Allow
                url.startsWith("https://") || url.startsWith("http://") -> false
                else -> false
            }
        }

        private fun injectMobileCSS(view: WebView?) {
            val css = """
                (function() {
                    var style = document.createElement('style');
                    style.innerHTML = `
                        :root { --qaxion-mobile: 1; }
                        body { -webkit-text-size-adjust: 100%; }
                        input, select, textarea { font-size: 16px !important; }
                        .btn, button { min-height: 44px !important; }
                        /* Hide desktop-only elements */
                        .desktop-only { display: none !important; }
                    `;
                    document.head.appendChild(style);
                    
                    /* Add mobile class to body */
                    document.body.classList.add('qaxion-mobile-app');
                })();
            """.trimIndent()
            view?.evaluateJavascript(css, null)
        }
    }

    // ─── WebChromeClient ──────────────────────────────────────
    inner class QAxionWebChromeClient : WebChromeClient() {

        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            binding.progressBar.progress = newProgress
            if (newProgress == 100) {
                binding.progressBar.visibility = View.GONE
            }
        }

        override fun onReceivedTitle(view: WebView?, title: String?) {
            if (!title.isNullOrEmpty() && title != "about:blank") {
                binding.tvSubtitle.text = title
            }
        }

        override fun onShowFileChooser(
            webView: WebView?,
            filePathCallback: ValueCallback<Array<Uri>>?,
            fileChooserParams: FileChooserParams?
        ): Boolean {
            uploadCallback?.onReceiveValue(null)
            uploadCallback = filePathCallback

            val intent = Intent(Intent.ACTION_CHOOSER).apply {
                val contentIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "*/*"
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }

                // Camera option
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { cIntent ->
                    val photoFile = createImageFile()
                    cameraImageUri = FileProvider.getUriForFile(
                        this@MainActivity,
                        "${packageName}.fileprovider",
                        photoFile
                    )
                    cIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                }

                putExtra(Intent.EXTRA_INTENT, contentIntent)
                putExtra(Intent.EXTRA_TITLE, "Selecionar Arquivo")
                putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
            }

            filePickerLauncher.launch(intent)
            return true
        }

        override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            MaterialAlertDialogBuilder(this@MainActivity, R.style.QAxionDialog)
                .setMessage(message)
                .setPositiveButton("OK") { _, _ -> result?.confirm() }
                .setOnCancelListener { result?.cancel() }
                .show()
            return true
        }

        override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
            MaterialAlertDialogBuilder(this@MainActivity, R.style.QAxionDialog)
                .setMessage(message)
                .setPositiveButton("Confirmar") { _, _ -> result?.confirm() }
                .setNegativeButton("Cancelar") { _, _ -> result?.cancel() }
                .setOnCancelListener { result?.cancel() }
                .show()
            return true
        }
    }

    private fun createImageFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile("QAxion_${timestamp}_", ".jpg", storageDir)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && binding.webView.canGoBack()) {
            binding.webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}
