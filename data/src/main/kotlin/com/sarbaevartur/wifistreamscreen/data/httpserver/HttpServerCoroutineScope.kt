package com.sarbaevartur.wifistreamscreen.data.httpserver

import com.elvishew.xlog.XLog
import com.sarbaevartur.wifistreamscreen.data.model.AppError
import com.sarbaevartur.wifistreamscreen.data.model.FatalError
import com.sarbaevartur.wifistreamscreen.data.other.getLog
import kotlinx.coroutines.*

abstract class HttpServerCoroutineScope(
    protected val onError: (AppError) -> Unit
) {

    protected val lock = Unit

    protected val coroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, throwable ->
            XLog.e(getLog("onCoroutineException"), throwable)
            onError(FatalError.CoroutineException)
        }
    )

    open fun destroy() {
        synchronized(lock) {
            XLog.d(getLog("destroy", "Invoked"))
            coroutineScope.cancel()
        }
    }
}