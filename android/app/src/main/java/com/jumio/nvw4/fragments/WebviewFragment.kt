package com.jumio.nvw4.fragments

import android.Manifest
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jumio.nvw4.R
import kotlinx.android.synthetic.main.fragment_webview.*


class WebviewFragment : Fragment() {

    companion object {
        var TAG: String = "NVW4"
        var PERMISSION_REQUEST_CODE: Int = 1000
        private var mUploadMessage: ValueCallback<Uri?>? = null
        var uploadMessage: ValueCallback<Array<Uri>>? = null
        const val REQUEST_SELECT_FILE = 1002
        private const val FILECHOOSER_RESULTCODE = 1003

        //Inject javascript code here that is executed after the page is loaded
        val injectFunction = """
        function () {
            window['__NVW_WEBVIEW__'] = {
            isAndroid: true
            }
        }
        """.trimIndent()

        private var mFilePathCallback: ValueCallback<Array<Uri>>? = null

        fun newInstance(url: String): WebviewFragment {
            val fragment = WebviewFragment()

            val args = Bundle()
            args.putString("url", url)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        webview.settings.javaScriptEnabled = true
        webview.settings.allowFileAccessFromFileURLs = true
        webview.settings.allowFileAccess = true
        webview.settings.allowContentAccess = true
        webview.settings.allowUniversalAccessFromFileURLs = true
        webview.settings.javaScriptCanOpenWindowsAutomatically = true
        webview.settings.mediaPlaybackRequiresUserGesture = false
        webview.settings.domStorageEnabled = true
        webview.addJavascriptInterface(PostMessageHandler(), "__NVW_WEBVIEW_HANDLER__")
        webview.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show()
            }

            override fun onReceivedSslError(
                view: WebView?,
                handler: SslErrorHandler?,
                error: SslError?
            ) {
                handler?.proceed()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                // Put your javascript function that you want to execute here
                webview.loadUrl("javascript:($injectFunction)()")
            }
        }
        webview.webChromeClient = object : WebChromeClient() {
            // Grant permissions for cam
            @TargetApi(Build.VERSION_CODES.M)
            override fun onPermissionRequest(request: PermissionRequest) {
                activity?.runOnUiThread {
                    if ("android.webkit.resource.VIDEO_CAPTURE" == request.resources[0]) {
                        if (ContextCompat.checkSelfPermission(
                                activity!!,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.d(
                                TAG,
                                String.format(
                                    "PERMISSION REQUEST %s GRANTED",
                                    request.origin.toString()
                                )
                            )
                            request.grant(request.resources)
                        } else {
                            ActivityCompat.requestPermissions(
                                activity!!,
                                arrayOf(
                                    Manifest.permission.CAMERA,
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                ),
                                PERMISSION_REQUEST_CODE
                            )
                        }
                    }
                }
            }

            //For Android 4.1 only
            protected fun openFileChooser(
                uploadMsg: ValueCallback<Uri?>,
                acceptType: String?,
                capture: String?
            ) {
                mUploadMessage = uploadMsg
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(intent, "File Browser"),
                    FILECHOOSER_RESULTCODE
                )
            }

            // For Lollipop 5.0+ Devices
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            override fun onShowFileChooser(
                mWebView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                try {
                    uploadMessage = filePathCallback

                    val intent = fileChooserParams.createIntent()
                    intent.type = "image/*"
                    try {
                        startActivityForResult(intent, REQUEST_SELECT_FILE)
                    } catch (e: ActivityNotFoundException) {
                        uploadMessage = null
                        Toast.makeText(
                            activity?.applicationContext,
                            "Cannot Open File Chooser",
                            Toast.LENGTH_LONG
                        ).show()
                        return false
                    }
                    return true
                } catch (e: ActivityNotFoundException) {
                    uploadMessage = null
                    Toast.makeText(
                        activity?.applicationContext,
                        "Cannot Open File Chooser",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                return true
            }

            protected fun openFileChooser(uploadMsg: ValueCallback<Uri?>) {
                mUploadMessage = uploadMsg
                val i = Intent(Intent.ACTION_GET_CONTENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.type = "image/*"
                startActivityForResult(
                    Intent.createChooser(i, "File Chooser"),
                    FILECHOOSER_RESULTCODE
                )
            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                Log.d(TAG, consoleMessage.message())
                return true
            }

            override fun getDefaultVideoPoster(): Bitmap {
                return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            }
        }
        webview.loadUrl(arguments?.get("url") as String)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return
                uploadMessage!!.onReceiveValue(
                    WebChromeClient.FileChooserParams.parseResult(
                        resultCode,
                        intent
                    )
                )
                uploadMessage = null
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return
            val result =
                if (intent == null || resultCode != AppCompatActivity.RESULT_OK) null else intent.data
            mUploadMessage!!.onReceiveValue(result)
            mUploadMessage = null
        } else {
            Toast.makeText(
                activity?.applicationContext,
                "Failed to Upload Image",
                Toast.LENGTH_LONG
            ).show()
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }
    class PostMessageHandler {
        @JavascriptInterface
        fun postMessage(json: String?, transferList: String?): Boolean {
            /*
                There we're handling messages from NVW4 client, its the same as for iFrame logging;
                More details you can find there:
                https://github.com/Jumio/implementation-guides/blob/master/netverify/netverify-web-v4.md#optional-iframe-logging
            */
            Log.d(TAG, "postMessage triggered, json: " + json.toString())
            return true
        }
    }
}