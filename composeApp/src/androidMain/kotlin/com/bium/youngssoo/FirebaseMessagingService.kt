package com.bium.youngssoo

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // 푸시 알림 처리
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 토큰 갱신 처리
    }
}
