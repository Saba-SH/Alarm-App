package ge.sshoshiashvili.alarmapp.receiver

import android.app.AlarmManager
import android.app.Notification.DEFAULT_VIBRATE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import ge.sshoshiashvili.alarmapp.R
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem
import ge.sshoshiashvili.alarmapp.main.MainActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            intent?.let {
                val alarmItem =
                    AlarmItem(
                        intent.getIntExtra("hour", -1),
                        intent.getIntExtra("minute", -1),
                        true
                    )
                alarmItem.id = intent.getLongExtra("id", -1)
                when (intent.action) {
                    "ge.sshoshiashvili.alarm.ACTION_ALARM" -> {
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                        val openAppIntent = PendingIntent.getActivity(
                            context,
                            MainActivity.ACTIVITY_LAUNCH_REQUEST_CODE,
                            Intent(context, MainActivity::class.java),
                            PendingIntent.FLAG_IMMUTABLE
                        )

                        val cancelAlarmIntent = Intent(context, this::class.java)
                        cancelAlarmIntent.action = "ge.sshoshiashvili.alarm.NOTIFICATION_CANCEL"
                        val cancelAlarmClick = PendingIntent.getBroadcast(
                            context, MainActivity.CANCEL_ALARM_CODE,
                            cancelAlarmIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )

                        val newAlarmIntent = Intent(context, this::class.java)
                        newAlarmIntent.action = "ge.sshoshiashvili.alarm.NOTIFICATION_SNOOZE"
                        newAlarmIntent.putExtra("id", alarmItem.id)
                        newAlarmIntent.putExtra("hour", alarmItem.hour)
                        newAlarmIntent.putExtra("minute", alarmItem.minute)

                        val snoozeAlarmClick = PendingIntent.getBroadcast(
                            context,
                            MainActivity.CANCEL_ALARM_CODE + alarmItem.id!!.toInt(),
                            newAlarmIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            val channel = NotificationChannel(
                                CHANNEL_ID,
                                "Channel name",
                                NotificationManager.IMPORTANCE_HIGH
                            )

                            notificationManager.createNotificationChannel(channel)
                        }

                        val hour = alarmItem.hour.toString().padStart(2, '0')
                        val minute = alarmItem.minute.toString().padStart(2, '0')
                        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                            .setContentIntent(openAppIntent)
                            .setSmallIcon(R.drawable.alarm_clock)
                            .setContentTitle("Alarm message!")
                            .setContentText("Alarm set on $hour:$minute")
                            .setDefaults(DEFAULT_VIBRATE)
                            .addAction(R.mipmap.ic_launcher, "Cancel", cancelAlarmClick)
                            .addAction(R.mipmap.ic_launcher, "Snooze", snoozeAlarmClick)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .build()
                        notificationManager.notify(NOTIFICATION_ID, notification)
                    }
                    "ge.sshoshiashvili.alarm.NOTIFICATION_CANCEL" -> {
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(NOTIFICATION_ID)
                    }
                    "ge.sshoshiashvili.alarm.NOTIFICATION_SNOOZE" -> {
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(NOTIFICATION_ID)
                        val newAlarmActionIntent = Intent(context, this::class.java)
                        newAlarmActionIntent.action = "ge.sshoshiashvili.alarm.ACTION_ALARM"
                        newAlarmActionIntent.putExtra("id", alarmItem.id)
                        newAlarmActionIntent.putExtra("hour", alarmItem.hour)
                        newAlarmActionIntent.putExtra("minute", alarmItem.minute + 1)

                        val newAlarmPendingIntent = PendingIntent.getBroadcast(
                            context,
                            MainActivity.ALARM_REQUEST_CODE_SNOOZED,
                            newAlarmActionIntent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        val alarmManager =
                            context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, alarmItem.hour)
                        calendar.set(Calendar.MINUTE, alarmItem.minute + 1)
                        calendar.set(Calendar.SECOND, 0)

                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            newAlarmPendingIntent
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "alarm_channel_id"
        private const val NOTIFICATION_ID = 100
    }

}