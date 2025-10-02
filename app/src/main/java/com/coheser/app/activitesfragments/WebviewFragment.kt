package com.coheser.app.activitesfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.coheser.app.R
import com.coheser.app.databinding.ActivityWebviewBinding
import com.coheser.app.interfaces.FragmentCallBack
import com.coheser.app.simpleclasses.Functions

class WebviewFragment(val callback: FragmentCallBack) : Fragment(), View.OnClickListener {

    var url: String? = "www.google.com"
    var title: String? = null

    lateinit var binding: ActivityWebviewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.activity_webview,container,false)

        url = requireArguments().getString("url")
        title =requireArguments().getString("title")
        if (title == getString(R.string.promote_video)) {
            binding.toolbar.visibility = View.GONE
        }
        Functions.printLog(com.coheser.app.Constants.tag, url)
        binding.goBack.setOnClickListener(this)
        binding.titleTxt!!.setText(title)
        binding.webview!!.setWebChromeClient(object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                if (progress >= 80) {
                    binding.progressBar!!.setVisibility(View.GONE)
                }
            }
        })
        binding.webview!!.getSettings().javaScriptEnabled = true
        binding.webview!!.loadUrl(url!!)
        binding.webview!!.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                view.loadUrl(url)
                if (url.equals("closePopup", ignoreCase = true)) {
                    requireActivity().onBackPressed()
                }
                return false
            }
        })


        return binding.root
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.goBack -> {
                callback.onResponce(Bundle())
                getFragmentManager()?.popBackStack()

            }
        }
    }

}
