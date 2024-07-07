package peavy

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class LifecycleListener(context: Context) : ActivityLifecycleCallbacks {
    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        (context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) {
        if (!Peavy.isInitialized) return

        scope.launch {
            Peavy.push.prepareAndPush()
        }
    }
}