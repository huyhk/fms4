package com.megatech.fms;

import android.app.NotificationManager;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FMSMessagingService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
