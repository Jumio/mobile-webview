package com.jumio.nvw4

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import com.jumio.nvw4.databinding.ActivityMainBinding
import com.jumio.nvw4.fragments.FragmentCallback
import com.jumio.nvw4.fragments.SettingsFragment
import com.jumio.nvw4.fragments.WebViewFragment

private const val PERMISSION_REQUEST_CODE: Int = 1001

class MainActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback, FragmentCallback {
	private lateinit var binding: ActivityMainBinding
	private var requestedUrl: String= ""

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
		arrayOf(
			Manifest.permission.CAMERA,
			Manifest.permission.READ_EXTERNAL_STORAGE,
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityMainBinding.inflate(layoutInflater)
		val view = binding.root
		val toolbar = binding.toolbar

		setContentView(view)
		setSupportActionBar(toolbar)

		if (savedInstanceState == null) {
			supportFragmentManager
				.beginTransaction()
				.add(R.id.fragment_container, SettingsFragment.newInstance())
				.commit()
		}
	}

	override fun showWebViewOrRequestPermission(url: String) {
		requestedUrl = url
		val missingPermissions = checkMissingPermissions(permissions)
		if (missingPermissions.isNotEmpty()) {
			ActivityCompat.requestPermissions(this, missingPermissions, PERMISSION_REQUEST_CODE)
		} else {
			showWebView(url)
		}
	}

	override fun backToSettings() {
		supportFragmentManager.popBackStack("webview", FragmentManager.POP_BACK_STACK_INCLUSIVE)
		openAppSettings()
	}

	private fun checkMissingPermissions(permissions: Array<String>): Array<String> = permissions.filter { permission ->
		ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
	}.toTypedArray()

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)

		if (requestCode == PERMISSION_REQUEST_CODE) {
			val deniedPermissions = permissions.filterIndexed { index, _ ->
				grantResults[index] != PackageManager.PERMISSION_GRANTED
			}

			if (deniedPermissions.isNotEmpty()) {
				handleDeniedPermissions(deniedPermissions)
			} else {
				showWebView(requestedUrl)
			}
		}
	}

	private fun handleDeniedPermissions(deniedPermissions: List<String>) {
		val permanentlyDenied = deniedPermissions.filter { permission ->
			!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
		}

		if (permanentlyDenied.isNotEmpty()) {
			// Permissions permanently denied - direct to settings
			AlertDialog.Builder(this)
				.setCancelable(false)
				.setTitle("Permissions Required")
				.setMessage("Some permissions were permanently denied. Please enable them in Settings to use this app.")
				.setPositiveButton("Open Settings") { dialog, _ ->
					openAppSettings()
					dialog.dismiss()
				}
				.setNegativeButton("Cancel") { dialog, _ ->
					dialog.dismiss()
				}
				.show()
		} else {
			showWebViewOrRequestPermission(requestedUrl)
		}
	}

	private fun showWebView(url: String) {
		if (url.isEmpty()) {
			return
		}
		window?.setFlags(
			WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
			WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
		)
		supportFragmentManager
			.beginTransaction()
			.replace(R.id.fragment_container, WebViewFragment.newInstance(url))
			.addToBackStack("webview").commitAllowingStateLoss()
	}

	private fun openAppSettings() {
		val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
		intent.data = android.net.Uri.fromParts("package", packageName, null)
		startActivity(intent)
	}
}
