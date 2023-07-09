package ge.sshoshiashvili.alarmapp.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import ge.sshoshiashvili.alarmapp.data.AlarmDatabase
import ge.sshoshiashvili.alarmapp.data.AlarmRepository
import ge.sshoshiashvili.alarmapp.data.AlarmRepositoryImpl
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AlarmRepository

    init {
        val alarmDao = AlarmDatabase.getInstance(application).alarmItemDao()

        repository = AlarmRepositoryImpl(alarmDao)
    }

    fun getAllAlarmItems(): LiveData<List<AlarmItem>> {
        return repository.getAllAlarmItems()
    }

    suspend fun getAlarmItemById(id: Long): AlarmItem? {
        return repository.getAlarmItemById(id)
    }

    fun addAlarmItem(item: AlarmItem): Long = runBlocking {
        repository.addAlarmItem(item)
    }

    fun updateAlarmItem(item: AlarmItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateAlarmItem(item)
    }

    fun deleteAlarmItem(item: AlarmItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteAlarmItem(item)
    }

}