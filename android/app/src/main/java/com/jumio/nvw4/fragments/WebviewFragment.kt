package com.jumio.nvw4.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ActivityNotFoundException
import android.content.Context
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
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jumio.nvw4.databinding.FragmentWebviewBinding

class WebviewFragment : Fragment() {
	private var _binding: FragmentWebviewBinding? = null
	private val binding get() = _binding!!

	companion object {
		var TAG: String = "NVW4"
		var PERMISSION_REQUEST_CODE: Int = 1000
		const val REQUEST_SELECT_FILE = 1002

		var uploadMessage: ValueCallback<Array<Uri>>? = null

		fun newInstance(url: String): WebviewFragment = WebviewFragment().apply {
			arguments = Bundle().apply {
				putString("url", url)
			}
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		_binding = FragmentWebviewBinding.inflate(inflater, container, false)
		return _binding?.root
	}

	@SuppressLint("SetJavaScriptEnabled")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		WebView.setWebContentsDebuggingEnabled(true)

		binding.webview.apply {
			settings.apply {
				allowFileAccess = true
				allowContentAccess = true
				domStorageEnabled = true
				javaScriptEnabled = true
				@Suppress("DEPRECATION")
				allowUniversalAccessFromFileURLs = true
				javaScriptCanOpenWindowsAutomatically = true
				mediaPlaybackRequiresUserGesture = false
			}

			addJavascriptInterface(PostMessageHandler(), "__NVW_WEBVIEW_HANDLER__")
		}

		binding.webview.webChromeClient = object : WebChromeClientFullScreen() {
			// Grant permissions for camera
			@TargetApi(Build.VERSION_CODES.M)
			override fun onPermissionRequest(request: PermissionRequest) {
				activity?.runOnUiThread {
					if ("android.webkit.resource.VIDEO_CAPTURE" == request.resources[0]) {
						if (ContextCompat.checkSelfPermission(
								requireActivity(),
								Manifest.permission.CAMERA
							) == PackageManager.PERMISSION_GRANTED
						) {
							Log.d(TAG, String.format("PERMISSION REQUEST %s GRANTED", request.origin.toString()))
							request.grant(request.resources)
						} else {
							val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
								arrayOf(
									Manifest.permission.CAMERA,
									Manifest.permission.READ_MEDIA_AUDIO,
									Manifest.permission.READ_MEDIA_IMAGES,
									Manifest.permission.READ_MEDIA_VIDEO,
									Manifest.permission.MODIFY_AUDIO_SETTINGS,
									Manifest.permission.RECORD_AUDIO,
								)
							} else {
								arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
							}

							ActivityCompat.requestPermissions(
								requireActivity(),
								permissions,
								PERMISSION_REQUEST_CODE,
							)
						}
					}
				}
			}

			// For Lollipop 5.0+ Devices
			override fun onShowFileChooser(
				mWebView: WebView?,
				filePathCallback: ValueCallback<Array<Uri>>?,
				fileChooserParams: FileChooserParams,
			): Boolean {
				uploadMessage?.onReceiveValue(null)
				uploadMessage = filePathCallback

				val intent = fileChooserParams.createIntent().apply {
					type = "image/*"
					putExtra(Intent.EXTRA_MIME_TYPES, fileChooserParams.acceptTypes)
				}
				return try {
					@Suppress("DEPRECATION")
					startActivityForResult(intent, REQUEST_SELECT_FILE)
					true
				} catch (e: ActivityNotFoundException) {
					uploadMessage = null
					showToast(requireContext(), "Cannot Open File Chooser")
					false
				}
			}

			override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
				Log.d(TAG, "console message: ${consoleMessage.message()}")
				return true
			}

			override fun getDefaultVideoPoster(): Bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
		}

		binding.webview.webViewClient = object : WebViewClient() {
			@RequiresApi(Build.VERSION_CODES.M)
			override fun onReceivedError(
				view: WebView?,
				request: WebResourceRequest?,
				error: WebResourceError?,
			) {
				showToast(requireContext(), "${error?.errorCode}: ${error?.description}")
			}

			@Deprecated("Deprecated in Java")
			override fun onReceivedError(
				view: WebView?,
				errorCode: Int,
				description: String?,
				failingUrl: String?,
			) {
				if (description != null) {
					showToast(requireContext(), description)
				}
			}

			@SuppressLint("WebViewClientOnReceivedSslError")
			override fun onReceivedSslError(
				view: WebView?,
				handler: SslErrorHandler?,
				error: SslError?,
			) {
				handler?.proceed()
			}

			override fun onPageStarted(
				view: WebView?,
				url: String?,
				favicon: Bitmap?,
			) {
				// Inject any javascript function you want to execute here

//                    val injectFunction = """
//                        function () {
//                            window['__NVW_WEBVIEW__'] = {
//                                isAndroid: true
//                            }
//                        }
//                        """.trimIndent()
//                    binding?.webview?.loadUrl("javascript:($injectFunction)()")
			}
		}

		binding.webview.loadUrl(arguments?.getString("url") as String)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	@Deprecated("Deprecated in Java")
	override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
		if (requestCode == REQUEST_SELECT_FILE) {
			if (uploadMessage == null || resultCode != AppCompatActivity.RESULT_OK) {
				return
			}

			val imageUris = WebChromeClient.FileChooserParams.parseResult(resultCode, intent)

			if (imageUris.isNullOrEmpty()) {
				activity?.applicationContext?.let {
					showToast(it, "Failed to retrieve image", Toast.LENGTH_LONG)
				}
			}

			uploadMessage?.onReceiveValue(imageUris)
			uploadMessage = null
		} else {
			activity?.applicationContext?.let {
				showToast(it, "Failed to retrieve image", Toast.LENGTH_LONG)
			}
		}

		@Suppress("DEPRECATION")
		super.onActivityResult(requestCode, resultCode, intent)
	}

	class PostMessageHandler {
		@JavascriptInterface
		fun postMessage(json: String?, transferList: String?): Boolean {
			/*
				Here we are listening to any messages from NVW4 client, its the same as for iFrame logging;
				More details can be found here:
				https://github.com/Jumio/implementation-guides/blob/master/netverify/netverify-web-v4.md#optional-iframe-logging
			 */
			Log.d(TAG, "postMessage triggered, json: $json")
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
		override fun onShowCustomView(
			view: View,
			callback: CustomViewCallback,
		) {
			// If we have custom view, that means that we are already in full screen, and need to go to original state
			if (customView != null) {
				onHideCustomView()
				return
			}

			// Go to full screen
			customView = view

			// We need to store certain parameters, so we can restore app state after we exit full screen mode
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				originalVisibility = activity?.window?.decorView?.visibility ?: View.INVISIBLE
				(activity?.window?.decorView as FrameLayout).addView(
					customView,
					FrameLayout.LayoutParams(-1, -1),
				)
				activity?.window?.setDecorFitsSystemWindows(false)
			} else {
				@Suppress("DEPRECATION")
				originalVisibility =
					activity?.window?.decorView?.windowSystemUiVisibility ?: View.INVISIBLE
				(activity?.window?.decorView as FrameLayout).addView(
					customView,
					FrameLayout.LayoutParams(-1, -1),
				)
				@Suppress("DEPRECATION")
				activity?.window?.decorView?.systemUiVisibility =
					3846 or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
			}

			originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
		}

		/**
		 * Callback will tell the host application that the current page exited full screen mode,
		 * and the app has to hide custom view.
		 */
		override fun onHideCustomView() {
			(activity?.window?.decorView as FrameLayout).removeView(
				customView,
			)
			customView = null
			// Restoring aps state, as it was before we go to full screen
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
				activity?.window?.setDecorFitsSystemWindows(true)
			} else {
				@Suppress("DEPRECATION")
				activity?.window?.decorView?.systemUiVisibility = originalVisibility
			}
			activity?.requestedOrientation = originalOrientation
			if (customViewCallback != null) customViewCallback?.onCustomViewHidden()
			customViewCallback = null
		}
	}
}

private fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
	Log.d(WebviewFragment.TAG, message)
	Toast.makeText(context, message, duration).show()
}

