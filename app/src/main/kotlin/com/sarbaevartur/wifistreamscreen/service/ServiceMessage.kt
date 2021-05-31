package com.sarbaevartur.wifistreamscreen.service

import com.sarbaevartur.wifistreamscreen.data.model.AppError
import com.sarbaevartur.wifistreamscreen.data.model.HttpClient
import com.sarbaevartur.wifistreamscreen.data.model.NetInterface
import com.sarbaevartur.wifistreamscreen.data.model.TrafficPoint

sealed class ServiceMessage {
    object FinishActivity : ServiceMessage()

    data class ServiceState(
        val isStreaming: Boolean, val isBusy: Boolean, val isWaitingForPermission: Boolean,
        val netInterfaces: List<NetInterface>,
        val appError: AppError?
    ) : ServiceMessage()

    data class Clients(val clients: List<HttpClient>) : ServiceMessage()
    data class TrafficHistory(val trafficHistory: List<TrafficPoint>) : ServiceMessage() {
        override fun toString(): String = this::class.java.simpleName
    }

    override fun toString(): String = this::class.java.simpleName
}