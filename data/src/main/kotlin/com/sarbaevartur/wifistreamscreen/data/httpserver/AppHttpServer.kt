package com.sarbaevartur.wifistreamscreen.data.httpserver

import com.sarbaevartur.wifistreamscreen.data.model.NetInterface

interface AppHttpServer {

    companion object {
        const val CLIENT_DISCONNECT_HOLD_TIME_SECONDS = 5
        const val TRAFFIC_HISTORY_SECONDS = 30
    }

    fun start(serverAddresses: List<NetInterface>, severPort: Int, useWiFiOnly: Boolean)

    fun stop()
}