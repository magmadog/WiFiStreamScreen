package com.sarbaevartur.wifistreamscreen.service.helper

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import com.elvishew.xlog.XLog
import com.sarbaevartur.wifistreamscreen.data.other.getLog
import com.sarbaevartur.wifistreamscreen.service.AppService
import com.sarbaevartur.wifistreamscreen.ui.activity.AppActivity
import kotlinx.android.parcel.Parcelize

sealed class IntentAction : Parcelable {
    internal companion object {
        private const val EXTRA_PARCELABLE = "EXTRA_PARCELABLE"
        fun fromIntent(intent: Intent?): IntentAction? = intent?.getParcelableExtra(EXTRA_PARCELABLE)
    }

    fun toAppServiceIntent(context: Context): Intent = AppService.getAppServiceIntent(context).apply {
        putExtra(EXTRA_PARCELABLE, this@IntentAction)
    }

    fun toAppActivityIntent(context: Context): Intent = AppActivity.getAppActivityIntent(context).apply {
        putExtra(EXTRA_PARCELABLE, this@IntentAction)
    }

    fun sendToAppService(context: Context) {
        XLog.i(context.getLog("sendToAppService", this.toString()))
        AppService.startForeground(context, this.toAppServiceIntent(context))
    }

    @Parcelize object GetServiceState : IntentAction()
    @Parcelize object StartStream : IntentAction()
    @Parcelize object StopStream : IntentAction()
    @Parcelize object Exit : IntentAction()
    @Parcelize data class CastIntent(val intent: Intent) : IntentAction()
    @Parcelize object CastPermissionsDenied : IntentAction()
    @Parcelize object StartOnBoot : IntentAction()
    @Parcelize object RecoverError : IntentAction()

    override fun toString(): String = this::class.java.simpleName
}