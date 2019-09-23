package com.senior.notification

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.senior.notification.ChannelManager.Companion.getChannelId
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init.setOnClickListener {
            ChannelManager.create(this)
        }

        update.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(
                RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI,
                Settings.System.DEFAULT_NOTIFICATION_URI
            )
            intent.putExtra(
                RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                Uri.parse(
                    defaultSharedPreferences.getString(
                        "RINGTONE",
                        Settings.System.DEFAULT_NOTIFICATION_URI.toString()
                    )
                )
            )
            startActivityForResult(intent, 1)
        }

        update_local.setOnClickListener {
            val uri = Uri.parse("android.resource://${this.packageName}/${R.raw.mixin}")
            defaultSharedPreferences.putString("RINGTONE", uri.toString())
            if (!ChannelManager.updateChannelSound(this, getChannelId(this), uri)) {
                ChannelManager.create(this)
            }
        }

        notify.setOnClickListener {
            val notificationBuilder = NotificationCompat.Builder(this, getChannelId(this))
            notificationBuilder.setTicker("ticker")
            notificationBuilder.setContentText("Content Text")
            notificationBuilder.setContentTitle("Content title")
            notificationBuilder.setWhen(System.currentTimeMillis())
            notificationBuilder.setSound(
                Uri.parse(
                    this.defaultSharedPreferences.getString(
                        "RINGTONE",
                        Settings.System.DEFAULT_NOTIFICATION_URI.toString()
                    )
                )
            )
            notificationBuilder.setAutoCancel(true)
            notificationBuilder.priority = NotificationCompat.PRIORITY_HIGH
            notificationBuilder.setSmallIcon(R.drawable.ic_msg_default)
            notificationManager.notify(Random.nextInt(1000), notificationBuilder.build())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let {
                defaultSharedPreferences.putString("RINGTONE", it.toString())
                if (!ChannelManager.updateChannelSound(this, getChannelId(this), it)) {
                    ChannelManager.create(this)
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
