package com.senior.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

class NotificationChannels {

    companion object {
        fun create(context: Context) {
            supportsOreo {
                val messageChannel =
                    NotificationChannel(
                        "CHANNEL_MESSAGE",
                        "Message",
                        NotificationManager.IMPORTANCE_HIGH
                    )

                messageChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                messageChannel.setSound(
                    Uri.parse(
                        context.defaultSharedPreferences.getString(
                            "RINGTONE",
                            Settings.System.DEFAULT_NOTIFICATION_URI.toString()
                        )
                    ),
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .build()
                )


                context.notificationManager.createNotificationChannel(
                        messageChannel
                )
            }
        }

        fun updateChannelSound(context: Context, id: String, uri: Uri): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existingChannel =
                    context.notificationManager.getNotificationChannel(id) ?: return false
                try {
                    context.notificationManager.deleteNotificationChannel(existingChannel.id)
                } catch (e: Exception) {
                    Log.e("___", e.localizedMessage)
                }
                val newChannel = copyChannel(existingChannel, id)
                newChannel.setSound(
                    uri,
                    AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .build()
                )
                context.notificationManager.createNotificationChannel(newChannel)
                return true
            } else {
                return false
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun copyChannel(original: NotificationChannel, id: String): NotificationChannel {
            val copy = NotificationChannel(id, original.name, original.importance)

            copy.group = original.group
            copy.setSound(original.sound, original.audioAttributes)
            copy.setBypassDnd(original.canBypassDnd())
            copy.enableVibration(original.shouldVibrate())
            copy.vibrationPattern = original.vibrationPattern
            copy.lockscreenVisibility = original.lockscreenVisibility
            copy.setShowBadge(original.canShowBadge())
            copy.lightColor = original.lightColor
            copy.enableLights(original.shouldShowLights())

            return copy
        }
    }
}
