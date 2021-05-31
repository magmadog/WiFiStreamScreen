package com.sarbaevartur.wifistreamscreen.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.elvishew.xlog.XLog
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.ktx.AppUpdateResult
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.requestUpdateFlow
import com.sarbaevartur.wifistreamscreen.data.other.getLog
import com.sarbaevartur.wifistreamscreen.data.settings.Settings
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject

abstract class AppUpdateActivity : BaseActivity() {

    companion object {
        private const val APP_UPDATE_PENDING_KEY = "info.dvkr.screenstream.key.APP_UPDATE_PENDING"
        private const val APP_UPDATE_FLEXIBLE_REQUEST_CODE = 15
    }

    protected val settings: Settings by inject()

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private var isAppUpdatePending: Boolean = false
    private var appUpdateConfirmationDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isAppUpdatePending = savedInstanceState?.getBoolean(APP_UPDATE_PENDING_KEY) ?: false
        XLog.d(getLog("onCreate", "isAppUpdatePending: $isAppUpdatePending"))

        appUpdateManager.requestUpdateFlow().onEach { updateResult ->
            if (isAppUpdatePending.not() && isIAURequestTimeoutPassed() &&
                updateResult is AppUpdateResult.Available && updateResult.updateInfo.isFlexibleUpdateAllowed
            ) {
                XLog.d(this@AppUpdateActivity.getLog("AppUpdateManager", "startUpdateFlowForResult"))
                isAppUpdatePending = true
                appUpdateManager.startUpdateFlowForResult(
                    updateResult.updateInfo, AppUpdateType.FLEXIBLE, this, APP_UPDATE_FLEXIBLE_REQUEST_CODE
                )
            }
        }
            .catch { throwable -> XLog.e(getLog("AppUpdateManager.catch: $throwable")) }
            .launchIn(lifecycleScope)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        XLog.d(getLog("onSaveInstanceState", "isAppUpdatePending: $isAppUpdatePending"))
        outState.putBoolean(APP_UPDATE_PENDING_KEY, isAppUpdatePending)
        super.onSaveInstanceState(outState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == APP_UPDATE_FLEXIBLE_REQUEST_CODE) {
            isAppUpdatePending = false
            if (resultCode == Activity.RESULT_OK) {
                XLog.d(getLog("onActivityResult", "Update permitted"))
            } else {
                XLog.d(getLog("onActivityResult", "Update canceled"))
                settings.lastIAURequestTimeStamp = System.currentTimeMillis()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun isIAURequestTimeoutPassed(): Boolean =
        // 8 hours. Don't need exact time frame
        System.currentTimeMillis() - settings.lastIAURequestTimeStamp >= 8 * 60 * 60 * 1000L
}