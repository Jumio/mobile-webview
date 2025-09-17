package com.jumio.nvw4.fragments

interface FragmentCallback {
	fun showWebViewOrRequestPermission(url: String)
	fun backToSettings()
}