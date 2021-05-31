package com.sarbaevartur.wifistreamscreen.ui.activity

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.elvishew.xlog.XLog
import com.sarbaevartur.wifistreamscreen.R
import com.sarbaevartur.wifistreamscreen.data.other.getLog
import com.sarbaevartur.wifistreamscreen.data.settings.Settings
import com.sarbaevartur.wifistreamscreen.data.settings.SettingsReadOnly
import com.sarbaevartur.wifistreamscreen.service.ServiceMessage
import com.sarbaevartur.wifistreamscreen.service.helper.IntentAction
import kotlinx.android.synthetic.main.activity_app.*

class AppActivity : PermissionActivity() {

    companion object {
        fun getAppActivityIntent(context: Context): Intent =
            Intent(context.applicationContext, AppActivity::class.java)

        fun getStartIntent(context: Context): Intent =
            getAppActivityIntent(context)
    }

    private val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.5f, 1f)
    private val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.5f, 1f)
    private val alpha = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    private var lastServiceMessage: ServiceMessage.ServiceState? = null
    private val settingsListener = object : SettingsReadOnly.OnSettingsChangeListener {
        override fun onSettingsChanged(key: String) {
            if (key == Settings.Key.LOGGING_ON) setLogging(settings.loggingOn)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        with(findNavController(R.id.fr_activity_app_nav_host_fragment)) {
            bottom_navigation_activity_app.setupWithNavController(this)
            addOnDestinationChangedListener { _, destination, _ ->
                if (destination.id == R.id.nav_exitFragment) IntentAction.Exit.sendToAppService(this@AppActivity)
            }
        }

        routeIntentAction(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        routeIntentAction(intent)
    }

    override fun onStart() {
        super.onStart()
        settings.registerChangeListener(settingsListener)
        setLogging(settings.loggingOn)
    }

    override fun onStop() {
        settings.unregisterChangeListener(settingsListener)
        super.onStop()
    }

    private fun routeIntentAction(intent: Intent?) {
        val intentAction = IntentAction.fromIntent(intent)
        intentAction != null || return
        XLog.d(getLog("routeIntentAction", "IntentAction: $intentAction"))

        when (intentAction) {
            IntentAction.StartStream -> IntentAction.StartStream.sendToAppService(this)
        }
    }

    private fun setLogging(loggingOn: Boolean) {
        ll_activity_app_logs.visibility = if (loggingOn) View.VISIBLE else View.GONE
        v_activity_app_logs.visibility = if (loggingOn) View.VISIBLE else View.GONE

    }

    @SuppressLint("RestrictedApi")
    override fun onServiceMessage(serviceMessage: ServiceMessage) {
        super.onServiceMessage(serviceMessage)

        when (serviceMessage) {
            is ServiceMessage.ServiceState -> {
                lastServiceMessage != serviceMessage || return
                XLog.d(this@AppActivity.getLog("onServiceMessage", "$serviceMessage"))

                bottom_navigation_activity_app.menu.findItem(R.id.menu_fab).title =
                    if (serviceMessage.isStreaming)
                        getString(R.string.bottom_menu_stop)
                    else
                        getString(R.string.bottom_menu_start)

                with(fab_activity_app_start_stop) {
                    visibility = View.VISIBLE
                    isEnabled = serviceMessage.isBusy.not()

                    if (serviceMessage.isStreaming) {
                        setImageResource(R.drawable.ic_fab_stop_24dp)
                        setOnClickListener { IntentAction.StopStream.sendToAppService(this@AppActivity) }
                    } else {
                        setImageResource(R.drawable.ic_fab_start_24dp)
                        setOnClickListener { IntentAction.StartStream.sendToAppService(this@AppActivity) }
                    }
                }

                if (serviceMessage.isStreaming != lastServiceMessage?.isStreaming) {
                    ObjectAnimator.ofPropertyValuesHolder(fab_activity_app_start_stop, scaleX, scaleY, alpha).apply {
                        interpolator = OvershootInterpolator()
                        duration = 750
                    }.start()
                }

                lastServiceMessage = serviceMessage
            }
        }
    }
}