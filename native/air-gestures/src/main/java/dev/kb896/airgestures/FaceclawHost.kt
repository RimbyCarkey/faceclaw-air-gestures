package dev.kb896.airgestures

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

/** Private in-process boundary: the combined app never broadcasts navigation or text. */
class FaceclawHost private constructor(private val context: Context) {
    interface Listener { fun onInput(input: String) }
    private var listener: Listener? = null
    fun setListener(value: Listener?) { listener = value }
    fun managementIntent() = Intent(context, MainActivity::class.java)
    fun setupIntent() = Intent(context, SetupActivity::class.java)
    private val prefs = context.getSharedPreferences("faceclaw-band-mode", Context.MODE_PRIVATE)
    fun isGlassesMode() = prefs.getBoolean("glasses", false)
    fun toggleGlassesMode() { setGlassesMode(!isGlassesMode()) }
    fun setGlassesMode(enabled: Boolean) {
        if (enabled == isGlassesMode()) return
        prefs.edit().putBoolean("glasses", enabled).apply()
        try {
            if (enabled) {
                val current = Profiles.load(context).picked
                if (current != "faceclaw.native") prefs.edit().putString("phoneProfile", current).apply()
                selectFaceclawProfile()
            } else {
                val profiles = Profiles.load(context).profiles
                val stored = prefs.getString("phoneProfile", "")
                val restore = profiles.firstOrNull { it.id == stored && it.id != "faceclaw.native" }
                    ?: profiles.firstOrNull { it.id != "faceclaw.native" }
                check(restore != null) { "Create a phone profile in Air Gestures before switching." }
                Profiles.update(context) { it.copy(picked = restore.id) }
                BandService.pickProfile(context, restore.id)
            }
        } catch (error: Exception) {
            prefs.edit().putBoolean("glasses", !enabled).apply()
            throw error
        }
        main.post { listener?.onInput("mode-changed") }
    }

    /** A profile picked in Air Gestures or by the band's profile controls. */
    fun profilePicked(previous: String, current: String) {
        val enabled = current == "faceclaw.native"
        if (enabled == isGlassesMode()) return
        val edit = prefs.edit().putBoolean("glasses", enabled)
        if (enabled && previous != "faceclaw.native") edit.putString("phoneProfile", previous)
        edit.apply()
        main.post { listener?.onInput("mode-changed") }
    }

    fun isRunning() = BandService.instance != null
    fun stop() { BandService.stop(context) }
    fun selectFaceclawProfile() {
        val mapping = Gesture.entries.associate { it.key to "" }.toMutableMap().apply {
            put("index_tap", "faceclaw.click"); put("middle_tap", "faceclaw.double-click")
            put("swipe_up", "faceclaw.swipe-up"); put("swipe_down", "faceclaw.swipe-down")
            put("swipe_left", "faceclaw.swipe-left"); put("swipe_right", "faceclaw.swipe-right")
            put("index_double", "faceclaw.keyboard"); put("index_hold", "faceclaw.toggle")
        }
        val list = Profiles.update(context) { before ->
            val found = before.profiles.firstOrNull { it.id == "faceclaw.native" }
            val profiles = if (found == null) before.profiles + Profile("faceclaw.native", "Faceclaw glasses", mapping, DialChoice.NONE.key) else before.profiles
            before.copy(profiles = profiles, picked = "faceclaw.native")
        }
        BandService.pickProfile(context, list.picked)
    }
    companion object {
        private var instance: FaceclawHost? = null
        private val main = Handler(Looper.getMainLooper())
        fun glassesMode(context: Context) = context.getSharedPreferences("faceclaw-band-mode", Context.MODE_PRIVATE).getBoolean("glasses", false)
        @JvmStatic @Synchronized fun getInstance(context: Context): FaceclawHost = instance ?: FaceclawHost(context.applicationContext).also { instance = it }
        fun dispatch(action: String): String? {
            val input = action.removePrefix("faceclaw.")
            if (input == "toggle") {
                val host = instance ?: return "Faceclaw is not running"
                main.post { runCatching { host.toggleGlassesMode() } }; return null
            }
            if (input !in setOf("click", "double-click", "swipe-up", "swipe-down", "swipe-left", "swipe-right", "keyboard")) return "Unsupported Faceclaw input"
            val host = instance ?: return "Faceclaw is not running"
            if (!host.isGlassesMode()) return "Phone mode is active"
            main.post { if (host.isGlassesMode()) host.listener?.onInput(input) }
            return null
        }
    }
}
