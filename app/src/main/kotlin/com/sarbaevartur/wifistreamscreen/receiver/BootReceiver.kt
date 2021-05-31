package com.sarbaevartur.wifistreamscreen.receiver


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.elvishew.xlog.XLog
import com.sarbaevartur.wifistreamscreen.data.other.getLog
import com.sarbaevartur.wifistreamscreen.data.settings.SettingsReadOnly
import com.sarbaevartur.wifistreamscreen.service.helper.IntentAction
import org.koin.core.KoinComponent
import org.koin.core.inject

class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val settingsReadOnly: SettingsReadOnly by inject()

    override fun onReceive(context: Context, intent: Intent) {
        XLog.d(getLog("onReceive", "Invoked"))

        if (settingsReadOnly.startOnBoot.not()) Runtime.getRuntime().exit(0)

        if (
            intent.action == "android.intent.action.BOOT_COMPLETED" ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            IntentAction.StartOnBoot.sendToAppService(context)
        }
    }
}