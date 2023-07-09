package ge.sshoshiashvili.alarmapp.main

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import ge.sshoshiashvili.alarmapp.R
import ge.sshoshiashvili.alarmapp.adapter.AlarmItemListenerInterface
import ge.sshoshiashvili.alarmapp.adapter.AlarmItemsAdapter
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem
import ge.sshoshiashvili.alarmapp.databinding.ActivityMainBinding
import ge.sshoshiashvili.alarmapp.receiver.AlarmReceiver

class MainActivity : AppCompatActivity(), AlarmItemListenerInterface,
    TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var alarmItemsAdapter: AlarmItemsAdapter
    private lateinit var alarmItemsRV: RecyclerView

    private lateinit var pendingIntentMap: MutableMap<Long, PendingIntent>

    private lateinit var alarmManager: AlarmManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.supportActionBar?.hide()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmItemsRV = binding.items

        alarmItemsAdapter = AlarmItemsAdapter(this)
        alarmItemsRV.adapter = alarmItemsAdapter

        updateTheme()

        pendingIntentMap = HashMap()

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[MainViewModel::class.java]

        restorePendingIntents()

        Thread {
            val allItems: MutableList<AlarmItem> = mutableListOf()
            val allAlarmItems = viewModel.getAllAlarmItems()
            runOnUiThread {
                allAlarmItems.observe(this) { value ->
                    allItems.clear()
                    allItems.addAll(value)
                    alarmItemsAdapter.allAlarmItems = allItems
                    alarmItemsAdapter.notifyDataSetChanged()
                }
            }
        }.start()

        binding.themeSwitcher.setOnClickListener {
            val setDarkTheme: Int =
                if (binding.themeSwitcher.text == resources.getString(R.string.switch_to_dark)) {
                    1
                } else {
                    0
                }
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(IS_DARK_THEME, setDarkTheme)
                .apply()
            updateTheme()
        }

        binding.adder.setOnClickListener {
            addAlarm()
        }

    }

    private fun updateTheme() {
        val sharedPreferences =
            getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        when (sharedPreferences.getInt(IS_DARK_THEME, -1)) {
            -1 -> {

                when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        binding.themeSwitcher.setText(R.string.switch_to_light)
                        binding.adder.setImageResource(R.drawable.light_add)
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        binding.themeSwitcher.setText(R.string.switch_to_dark)
                        binding.adder.setImageResource(R.drawable.add)
                    }
                }
            }
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.themeSwitcher.setText(R.string.switch_to_dark)
                binding.adder.setImageResource(R.drawable.add)
            }
            else -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.themeSwitcher.setText(R.string.switch_to_light)
                binding.adder.setImageResource(R.drawable.light_add)
            }
        }
    }

    private fun addAlarm() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            this,
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun setAlarm(alarm: AlarmItem) {
        if (alarm.id == null) {
            Log.e("ERROR", "SET ALARM ID IS NULL")
            return
        }
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, alarm.hour)
        calendar.set(Calendar.MINUTE, alarm.minute)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        intent.action = "ge.sshoshiashvili.alarm.ACTION_ALARM"
        intent.putExtra("id", alarm.id)
        intent.putExtra("hour", alarm.hour)
        intent.putExtra("minute", alarm.minute)

        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            ALARM_REQUEST_CODE_BASE + (alarm.id!!.toInt()),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntentMap[alarm.id!!] = pendingIntent

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntentMap[alarm.id]
        )
    }

    private fun unsetAlarm(alarm: AlarmItem) {
        if (alarm.id == null) {
            Log.e("ERROR", "UNSET ALARM ID IS NULL")
            return
        }

        alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        if (pendingIntentMap[alarm.id!!] != null)
            alarmManager.cancel(pendingIntentMap[alarm.id!!])
    }

    override fun onCheckboxClick(position: Int, item: AlarmItem) {
        Thread {
            val newItem = AlarmItem(item.hour, item.minute, !item.active)
            newItem.id = item.id
            viewModel.updateAlarmItem(newItem)
            runOnUiThread {
                alarmItemsAdapter.notifyItemChanged(position)
            }

            if (newItem.active) {
                setAlarm(newItem)
            } else {
                unsetAlarm(newItem)
            }
        }.start()
    }

    override fun onItemLongClick(position: Int, item: AlarmItem) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_question)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                Thread {
                    viewModel.deleteAlarmItem(item)
                    runOnUiThread {
                        alarmItemsAdapter.allAlarmItems.removeAt(position)
                        alarmItemsAdapter.notifyItemRemoved(position)
                    }

                    if (item.active) {
                        unsetAlarm(item)
                    }
                }.start()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onTimeSet(timePicker: TimePicker?, hour: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        if (hour < currentHour || (hour == currentHour && minute < currentMinute)) {
            Toast.makeText(this, "Invalid time selection", Toast.LENGTH_SHORT).show()
            addAlarm()
        } else {
            val alarmItem = AlarmItem(hour, minute, true)
            Thread {
                alarmItem.id = viewModel.addAlarmItem(alarmItem)
                runOnUiThread {
                    alarmItemsAdapter.notifyItemInserted(alarmItemsAdapter.itemCount)
                    setAlarm(alarmItem)
                }
            }.start()
        }
    }

    private fun restorePendingIntents() {
        Thread {
            val activeAlarms = viewModel.getAllAlarmItems()
            runOnUiThread {
                activeAlarms.observe(this) { value ->
                    val calendar = Calendar.getInstance()
                    val filteredValue =
                        value.filter {
                            it.active && (it.hour > calendar.get(Calendar.HOUR_OF_DAY) ||
                                    (it.hour == calendar.get(Calendar.HOUR_OF_DAY) &&
                                            it.minute > calendar.get(Calendar.MINUTE)
                                            )
                                    )
                        }
                    for (value_ in filteredValue) {
                        val intent = Intent(applicationContext, AlarmReceiver::class.java)
                        intent.action = "ge.sshoshiashvili.alarm.ACTION_ALARM"
                        intent.putExtra("id", value_.id)
                        intent.putExtra("hour", value_.hour)
                        intent.putExtra("minute", value_.minute)

                        val pendingIntent = PendingIntent.getBroadcast(
                            applicationContext,
                            ALARM_REQUEST_CODE_BASE + (value_.id!!.toInt()),
                            intent,
                            PendingIntent.FLAG_IMMUTABLE
                        )
                        pendingIntentMap[value_.id!!] = pendingIntent
                    }
                }
            }
        }.start()
    }

    companion object {
        private const val PREFS_NAME = "alarm_app_preferences"
        private const val IS_DARK_THEME = "alarm_app_theme_is_dark"
        const val ALARM_REQUEST_CODE_BASE = 1980
        const val ALARM_REQUEST_CODE_SNOOZED = 1979
        const val ACTIVITY_LAUNCH_REQUEST_CODE = 1300
        const val CANCEL_ALARM_CODE = 1200
    }
}
