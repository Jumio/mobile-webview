package com.jumio.nvw4.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
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
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jumio.nvw4.databinding.FragmentWebviewBinding


class WebviewFragment : Fragment() {

    companion object {
        var TAG: String = "NVW4"
        var PERMISSION_REQUEST_CODE: Int = 1000
        private var mUploadMessage: ValueCallback<Uri?>? = null
        var uploadMessage: ValueCallback<Array<Uri>>? = null
        const val REQUEST_SELECT_FILE = 1002
        private const val FILECHOOSER_RESULTCODE = 1003

        private var _binding: FragmentWebviewBinding? = null
        private val binding get() = _binding!!


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
    ): View {
        _binding = FragmentWebviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WebView.setWebContentsDebuggingEnabled(true)

        binding.webview.settings.javaScriptEnabled = true
        binding.webview.settings.allowFileAccessFromFileURLs = true
        binding.webview.settings.allowFileAccess = true
        binding.webview.settings.allowContentAccess = true
        binding.webview.settings.allowUniversalAccessFromFileURLs = true
        binding.webview.settings.javaScriptCanOpenWindowsAutomatically = true
        binding.webview.settings.mediaPlaybackRequiresUserGesture = false
        binding.webview.settings.domStorageEnabled = true

        binding.webview.addJavascriptInterface(PostMessageHandler(), "__NVW_WEBVIEW_HANDLER__")

        binding.webview.webChromeClient = object : WebChromeClientFullScreen() {

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
            }

            private fun openFileChooser(uploadMsg: ValueCallback<Uri?>) {
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

        binding.webview.webViewClient = object : WebViewClient() {

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
                binding.webview.loadUrl("javascript:($injectFunction)()")
            }
        }

        binding.webview.loadUrl(arguments?.get("url") as String)
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

    open inner class WebChromeClientFullScreen : WebChromeClient() {
        private var customView: View? = null
        private var customViewCallback: CustomViewCallback? = null
        private var originalOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        private var originalVisibility = View.INVISIBLE

        /**
         * Callback will tell the host application that the current page would
         * like to show a custom View in a particular orientation
         */
        override fun onShowCustomView(view: View, callback: CustomViewCallback) {
            //If we have custom view, that means that we are already in full screen, and need to go to original state
            if (customView != null) {
                onHideCustomView()
                return
            }
            //going full screen
            customView = view
            //We need to store there parameters, so we can restore app state, after we exit full screen mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                originalVisibility = activity?.window?.decorView?.visibility!!
                (activity?.window?.decorView as FrameLayout).addView(
                    customView,
                    FrameLayout.LayoutParams(-1, -1)
                )
                activity?.window?.setDecorFitsSystemWindows(false)
            } else {
                originalVisibility = activity?.window?.decorView?.windowSystemUiVisibility!!
                (activity?.window?.decorView as FrameLayout).addView(
                    customView,
                    FrameLayout.LayoutParams(-1, -1)
                )
                activity?.window?.decorView?.systemUiVisibility =
                    3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }
            originalOrientation = activity?.requestedOrientation!!
        }

        /**
         * Callback will tell the host application that the current page exited full screen mode,
         * and the app has to hide custom view.
         */
        override fun onHideCustomView() {
            (activity?.window?.decorView as FrameLayout).removeView(
                customView
            )
            customView = null
            //Restoring aps state, as it was before we go to full screen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.window?.setDecorFitsSystemWindows(true)
            } else {
                activity?.window?.decorView?.systemUiVisibility = originalVisibility
            }
            activity?.requestedOrientation = originalOrientation
            if (customViewCallback != null) customViewCallback!!.onCustomViewHidden()
            customViewCallback = null
        }
    }
}