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

class ChannelManager {

    companion object {
        fun create(context: Context) {
            supportsOreo {
                val messageChannel =
                    NotificationChannel(
                        getChannelId(context),
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

        fun getChannelId(context: Context): String {
            return "MESSAGE_${context.defaultSharedPreferences.getInt("Channel_Version", 0)}"
        }

        @Synchronized
        fun updateChannelSound(context: Context, id: String, uri: Uri): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val existingChannel =
                    context.notificationManager.getNotificationChannel(id) ?: return false
                try {
                    context.notificationManager.deleteNotificationChannel(existingChannel.id)
                } catch (e: Exception) {
                    Log.e("___", e.localizedMessage)
                }
                val oldVersion = context.defaultSharedPreferences.getInt("Channel_Version", 0)
                val newChannel = copyChannel(
                    existingChannel,
                    "MESSAGE_${oldVersion + 1}"
                )

                context.defaultSharedPreferences.putInt("Channel_Version", oldVersion + 1)
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
