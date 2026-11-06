package com.faceclaw.app

import android.app.Application
import android.content.Context
import android.os.Build
import android.webkit.WebView
import com.tns.NativeScriptApplication

/** Keep the native-only enrollment process out of NativeScript and its WebView store. */
class FaceclawApplication : NativeScriptApplication() {
    private var enrollmentProcess = false

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        enrollmentProcess = Build.VERSION.SDK_INT >= 28 &&
            Application.getProcessName() == "${base.packageName}:band_enrollment"
        if (enrollmentProcess) {
            // Before providers, Application.onCreate, CookieManager, or a WebView instance.
            WebView.setDataDirectorySuffix("band-enrollment")
        }
    }

    override fun onCreate() {
        // The parent starts JS; this secondary process hosts only the native enrollment UI.
        if (!enrollmentProcess) super.onCreate()
    }
}
