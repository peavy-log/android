package peavy

import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.app.NotificationManagerCompat
import peavy.constants.EventState

internal fun Peavy.sendState(context: Context) {
    attempt {
        val uiMode = context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)
        state(EventState.UiTheme, if (uiMode == Configuration.UI_MODE_NIGHT_YES) "dark" else "light")
    }

    attempt { state(EventState.NetworkType, getNetwork(context)) }

    attempt {
        val notifs = NotificationManagerCompat.from(context).areNotificationsEnabled()
        state(EventState.Notifications, if (notifs) "enabled" else "disabled")
    }
    attempt { state(EventState.AvailableMemory, getMemory(context)) }
    attempt { state(EventState.PeavyVersion, BuildConfig.VERSION) }
    attempt { state(EventState.PlatformVersion, Build.VERSION.SDK_INT) }
    attempt {
        val version = getAppVersion(context)
        state(EventState.AppVersion, version.first)
        state(EventState.AppVersionCode, version.second)
    }
    attempt { state(EventState.DeviceModel, "${Build.MANUFACTURER} ${Build.MODEL}") }
    attempt {
        state(
            EventState.DeviceLanguage,
            context.resources.configuration.locale.let { "${it.language}-${it.country}" })
    }
    attempt { state(EventState.DeviceScreenWidth, context.resources.configuration.screenWidthDp) }
    attempt { state(EventState.DeviceScreenHeight, context.resources.configuration.screenHeightDp) }
}

private fun attempt(fn: () -> Unit) {
    try {
        fn()
    } catch (e: Exception) {
        Debug.warnSome("Failed to collect state", e)
    }
}

private fun getNetwork(context: Context): String {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val dataType = if (Build.VERSION.SDK_INT < 24) {
        val info = connectivityManager.activeNetworkInfo
        if (info == null || !info.isConnected) return "none"
        else if (info.type == ConnectivityManager.TYPE_WIFI) return "wifi"
        else if (info.type == ConnectivityManager.TYPE_ETHERNET) return "ethernet"
        else if (info.type == ConnectivityManager.TYPE_MOBILE) {
            info.subtype
        } else {
            return "unknown"
        }
    } else {
        val nw = connectivityManager.activeNetwork ?: return "none"
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return "none"
        when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> return "wifi"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> return "ethernet"
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                tm.dataNetworkType
            }

            else -> return "unknown"
        }
    }

    return when (dataType) {
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN,
        TelephonyManager.NETWORK_TYPE_GSM -> "2g"

        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3g"

        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_IWLAN, 19 -> "4g"

        TelephonyManager.NETWORK_TYPE_NR -> "5g"
        else -> "unknown"
    }
}

private fun getMemory(context: Context): Long {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memory = ActivityManager.MemoryInfo()
    activityManager.getMemoryInfo(memory)
    return memory.totalMem
}

private fun getAppVersion(context: Context): Pair<String, Int> {
    val info = context.packageManager.getPackageInfo(context.packageName, 0)
    return Pair(info.versionName ?: "unknown", info.versionCode)
}