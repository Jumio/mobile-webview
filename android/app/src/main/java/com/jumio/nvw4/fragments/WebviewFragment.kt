package com.jumio.nvw4.fragments

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jumio.nvw4.R
import kotlinx.android.synthetic.main.fragment_webview.*
import android.os.Build
import android.annotation.TargetApi
import android.util.Log
import android.net.http.SslError
import android.webkit.*
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.core.app.ActivityCompat
import android.graphics.Bitmap

class WebviewFragment : Fragment() {

    companion object {
        var TAG : String = "NVW4"
        var CAMERA_PERMISSION_REQUEST_CODE : Int = 1001

        fun newInstance(url:String): WebviewFragment {
            val fragment = WebviewFragment()

            val args = Bundle()
            args.putString("url", url)
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        if(ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity!!,  arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }

        WebView.setWebContentsDebuggingEnabled(true)

        webview.settings.javaScriptEnabled = true
        webview.settings.allowFileAccessFromFileURLs = true
        webview.settings.allowUniversalAccessFromFileURLs = true
        webview.settings.mediaPlaybackRequiresUserGesture = false
        webview.settings.domStorageEnabled = true
        webview.webViewClient = object : WebViewClient() {
            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                handler?.proceed()
            }
        }
        webview.webChromeClient = object : WebChromeClient() {
            // Grant permissions for cam
            @TargetApi(Build.VERSION_CODES.M)
            override fun onPermissionRequest(request: PermissionRequest) {
                activity?.runOnUiThread {
                    if("android.webkit.resource.VIDEO_CAPTURE" == request.resources[0]) {
                        if(ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            Log.d(TAG, String.format("PERMISSION REQUEST %s GRANTED", request.origin.toString()))
                            request.grant(request.resources)
                        } else {
                            ActivityCompat.requestPermissions(activity!!,  arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                        }
                    }
                }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(activity!!, "Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity!!, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}