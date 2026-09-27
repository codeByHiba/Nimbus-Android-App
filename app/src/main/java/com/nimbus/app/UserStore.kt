package com.nimbus.app

import android.content.Context
import org.json.JSONObject

/**
 * Very small local "backend" for this demo app.
 *
 * In a real production app you would replace this with network calls to your
 * own authentication server (or a service like Firebase Auth). Here we just
 * persist users in SharedPreferences so the Register -> Login -> Home flow
 * works end-to-end without any external dependency.
 */
data class NimbusUser(
    val name: String,
    val email: String,
    val phone: String,
    val password: String
)

object UserStore {

    private const val PREFS_NAME = "nimbus_prefs"
    private const val KEY_USERS = "users_json"
    private const val KEY_REMEMBER = "remembered_email"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun usersJson(context: Context): JSONObject {
        val raw = prefs(context).getString(KEY_USERS, null) ?: return JSONObject()
        return JSONObject(raw)
    }

    fun userExists(context: Context, email: String): Boolean =
        usersJson(context).has(email.lowercase())

    fun saveUser(context: Context, user: NimbusUser) {
        val users = usersJson(context)
        val entry = JSONObject()
        entry.put("name", user.name)
        entry.put("phone", user.phone)
        entry.put("password", user.password)
        users.put(user.email.lowercase(), entry)
        prefs(context).edit().putString(KEY_USERS, users.toString()).apply()
    }

    fun getUser(context: Context, email: String): NimbusUser? {
        val users = usersJson(context)
        val key = email.lowercase()
        if (!users.has(key)) return null
        val entry = users.getJSONObject(key)
        return NimbusUser(
            name = entry.getString("name"),
            email = email,
            phone = entry.getString("phone"),
            password = entry.getString("password")
        )
    }

    fun setRemembered(context: Context, email: String?) {
        prefs(context).edit().putString(KEY_REMEMBER, email).apply()
    }

    fun getRemembered(context: Context): String? =
        prefs(context).getString(KEY_REMEMBER, null)
}
