package com.faceclaw.app
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import android.webkit.CookieManager
import dev.kb896.airgestures.Bands
import dev.kb896.airgestures.Actions
import dev.kb896.airgestures.Bridge
import dev.kb896.airgestures.FaceclawHost
import dev.kb896.airgestures.Profiles
import dev.kb896.airgestures.Prefs
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
class NativeBandInstrumentation : Instrumentation() {
 override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
 override fun onStart() {
  val results = mutableListOf<String>()
  try {
   check(Build.HARDWARE in setOf("ranchu", "goldfish"))
   val pkg = targetContext.packageName
   for (permission in listOf("android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT", "android.permission.POST_NOTIFICATIONS")) uiAutomation.grantRuntimePermission(pkg, permission)
   check(Bridge.profileReport("{}").isNotBlank())
   results += "PASS: full Air Gestures ARM64 JNI loads"
   lateinit var host: FaceclawHost
   val phone = Profiles.load(targetContext).picked
   Profiles.update(targetContext) { it.copy(apps = it.apps + ("dev.fixture.phone" to phone)) }
   val phoneDraw = JSONObject(Prefs(targetContext).profilesJson()).getJSONObject("band").getJSONObject("draw")
   check(phoneDraw.getString("gesture") == "index_hold")
   val originalMap = Profiles.load(targetContext).profiles.first { it.id == phone }.map
   runOnMainSync { host = FaceclawHost.getInstance(targetContext); host.setGlassesMode(true) }
   check(host.isGlassesMode()); check(Profiles.load(targetContext).picked == "faceclaw.native")
   check(JSONObject(Prefs(targetContext).profilesJson()).getJSONObject("apps").length() == 0)
   check(JSONObject(Prefs(targetContext).profilesJson()).getJSONObject("band").isNull("draw"))
   check(Profiles.load(targetContext).apps["dev.fixture.phone"] == phone)
   runOnMainSync { check(Actions(targetContext).run("faceclaw.toggle") == null) }
   SystemClock.sleep(200)
   check(!host.isGlassesMode()); check(Profiles.load(targetContext).picked == phone)
   check(JSONObject(Prefs(targetContext).profilesJson()).getJSONObject("apps").has("dev.fixture.phone"))
   check(JSONObject(Prefs(targetContext).profilesJson()).getJSONObject("band").getJSONObject("draw").toString() == phoneDraw.toString())
   results += "PASS: index hold is released from Draw in glasses mode and restored in phone mode"
   results += "PASS: glasses mode suppresses automatic phone-profile takeover without losing its associations"
   check(Profiles.load(targetContext).profiles.first { it.id == phone }.map == originalMap)
   results += "PASS: native toggle restores the original phone profile without overwriting mappings"
   runOnMainSync {
    val slot = Bands.selected(targetContext)
    Bands.pick(targetContext, slot, "faceclaw.native")
    check(host.isGlassesMode())
    Bands.pick(targetContext, slot, phone)
    check(!host.isGlassesMode())
   }
   results += "PASS: Air Gestures profile picks synchronize Faceclaw mode in both directions"
   val received = CountDownLatch(1)
   var value = ""
   runOnMainSync {
    host.setGlassesMode(true)
    host.setListener(object : FaceclawHost.Listener { override fun onInput(input: String) { if (input != "mode-changed") { value = input; received.countDown() } } })
    check(Actions(targetContext).run("faceclaw.click") == null)
   }
   check(received.await(3, TimeUnit.SECONDS)); check(value == "click")
   runOnMainSync { host.setGlassesMode(false); host.setListener(null); check(Actions(targetContext).run("faceclaw.click") != null) }
   results += "PASS: in-process band actions dispatch only in glasses mode"
   targetContext.getSharedPreferences("prefs.db", 0).edit().putBoolean("onboarding.complete", true).putBoolean("onboarding.previewOnly", true).commit()
   targetContext.startActivity(Intent().setClassName(pkg, "com.tns.NativeScriptActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
   click("Band"); awaitText("Phone mode · Air Gestures controls")
   click("Toggle glasses / phone mode"); awaitText("Glasses mode · native band input")
   click("Toggle glasses / phone mode"); awaitText("Phone mode · Air Gestures controls")
   results += "PASS: Band tab beside Watch provides independent live mode controls"
   click("Band settings & Air Gestures"); awaitText("Native Neural Band controls")
   click("Toggle glasses / phone mode"); awaitText("Glasses mode enabled.")
   click("Toggle glasses / phone mode"); awaitText("Phone mode restored.")
   click("Air Gestures · all features"); awaitText("Gestures")
   uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
   awaitText("Native Neural Band controls")
   click("Enrollment, import and export"); awaitText("Owner key")
   uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
   awaitText("Native Neural Band controls")
   results += "PASS: unified app exposes mode controls, full native manager and enrollment screens"
   val ready = CountDownLatch(1)
   runOnMainSync { CookieManager.getInstance().setCookie("https://faceclaw.example", "isolation_probe=keep; Secure") { ready.countDown() } }
   check(ready.await(5, TimeUnit.SECONDS))
   targetContext.startActivity(Intent().setClassName(pkg, "dev.kb896.airgestures.MetaSignIn").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
   SystemClock.sleep(1000); assertCookie()
   uiAutomation.performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK)
   SystemClock.sleep(300); assertCookie()
   results += "PASS: Meta sign-in cancellation preserves Faceclaw WebView cookies"
   finish(Activity.RESULT_OK, Bundle().apply { putString("stream", "\n${results.joinToString("\n")}\n") })
  } catch (error: Throwable) { finish(Activity.RESULT_CANCELED, Bundle().apply { putString("stream", "\n${results.joinToString("\n")}\nFAIL: ${error.javaClass.simpleName}: ${error.message}\n") }) }
 }
    private fun assertCookie() {
        var cookie: String? = null
        runOnMainSync { cookie = CookieManager.getInstance().getCookie("https://faceclaw.example") }
        check(cookie?.contains("isolation_probe=keep") == true) { "Main WebView cookie was cleared by enrollment." }
    }

    private fun find(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.text?.toString()?.equals(text, ignoreCase = true) == true ||
            node.contentDescription?.toString()?.equals(text, ignoreCase = true) == true) return node
        for (index in 0 until node.childCount) find(node.getChild(index), text)?.let { return it }
        return null
    }

    private fun awaitText(text: String) {
        val until = SystemClock.uptimeMillis() + 15000
        while (SystemClock.uptimeMillis() < until) {
            if (find(uiAutomation.rootInActiveWindow, text) != null) return
            SystemClock.sleep(100)
        }
        error("Screen text not found: $text")
    }

    private fun awaitAbsent(text: String) {
        val until = SystemClock.uptimeMillis() + 10000
        while (SystemClock.uptimeMillis() < until) {
            if (find(uiAutomation.rootInActiveWindow, text) == null) return
            SystemClock.sleep(100)
        }
        error("Screen did not close: $text")
    }

    private fun click(text: String) {
        awaitText(text)
        var node = find(uiAutomation.rootInActiveWindow, text) ?: error("Button not found: $text")
        while (true) {
            if (node.isClickable && node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return
            node = node.parent ?: error("Button did not accept click: $text")
        }
    }
}
