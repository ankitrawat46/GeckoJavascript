package com.learn.geckojavascript

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.WebExtension

/*
* I need to run my javascript (assets/messaging/custom.js) on load of https://en.m.wikipedia.org/wiki/JavaScript URL.
* I tried using web extensions but didn't succeed
*
*
* */

class ActivityMain : AppCompatActivity() {
    private lateinit var context: Context
    private lateinit var session: GeckoSession
    private var runtime: GeckoRuntime? = null
    private var mPort: WebExtension.Port? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this

        session = GeckoSession()
        if (runtime == null) {
            runtime = GeckoRuntime.create(context)
        }

        val portDelegate = object : WebExtension.PortDelegate {
            var port: WebExtension.Port? = null
            override fun onPortMessage(message: Any, port: WebExtension.Port) {
                Log.e("PortDelegate", "Received message from WebExtension: $message")
            }

            override fun onDisconnect(port: WebExtension.Port) {
                if (port === mPort) {
                    mPort = null
                }
            }
        }

        val messageDelegate = object : WebExtension.MessageDelegate {
            override fun onConnect(port: WebExtension.Port) {
                mPort = port
                mPort?.setDelegate(portDelegate)
            }
        }

        val extension = WebExtension("resource://android/assets/messaging/")

        extension.setMessageDelegate(messageDelegate, "browser")
        val geckoResult = runtime?.registerWebExtension(extension)
        geckoResult?.exceptionally {
            Log.e("MessageDelegate", "Error registering WebExtension", it)
            return@exceptionally geckoResult
        }

        val settings = session.settings
        settings.allowJavascript = true
        session.progressDelegate = createProgressDelegate()
        session.open(runtime!!)
        geckoView.setSession(session)
        session.loadUri("https://en.m.wikipedia.org/wiki/JavaScript")
    }

    private fun createProgressDelegate(): GeckoSession.ProgressDelegate {
        return object : GeckoSession.ProgressDelegate {

            override fun onPageStop(session: GeckoSession, success: Boolean) {
                progressBar.visibility = View.GONE
                if (mPort != null) {
                    val message = JSONObject()
                    try {
                        message.put("value", "Testing Extension")
                    } catch (ex: JSONException) {
                        throw RuntimeException(ex);
                    }

                    mPort!!.postMessage(message);
                }
            }

            override fun onSecurityChange(
                session: GeckoSession,
                securityInfo: GeckoSession.ProgressDelegate.SecurityInformation
            ) = Unit

            override fun onPageStart(session: GeckoSession, url: String) {
                progressBar.visibility = View.VISIBLE
            }

            override fun onProgressChange(session: GeckoSession, progress: Int) {
            }
        }
    }
}