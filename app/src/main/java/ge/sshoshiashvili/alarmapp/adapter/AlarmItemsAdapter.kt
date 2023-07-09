package ge.sshoshiashvili.alarmapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ge.sshoshiashvili.alarmapp.data.entity.AlarmItem
import ge.sshoshiashvili.alarmapp.databinding.AlarmItemBinding

class AlarmItemsAdapter(
    private val alarmItemListener: AlarmItemListenerInterface,
    var allAlarmItems: MutableList<AlarmItem> = mutableListOf()
) :
    RecyclerView.Adapter<AlarmItemsAdapter.AlarmItemViewHolder>() {

    inner class AlarmItemViewHolder(binding: AlarmItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val timeTv = binding.tvTime
        val switch = binding.swSwitch
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlarmItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AlarmItemBinding.inflate(layoutInflater, parent, false)
        return AlarmItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmItemsAdapter.AlarmItemViewHolder, position: Int) {
        val hour = allAlarmItems[position].hour.toString().padStart(2, '0')
        val minute = allAlarmItems[position].minute.toString().padStart(2, '0')
        val time = "$hour:$minute"

        holder.timeTv.text = time
        holder.switch.isChecked = allAlarmItems[position].active

        holder.switch.setOnClickListener {
            alarmItemListener.onCheckboxClick(position, allAlarmItems[position])
        }

        holder.itemView.setOnLongClickListener {
            alarmItemListener.onItemLongClick(position, allAlarmItems[position])
            true
        }
    }

    override fun getItemCount(): Int {
        return allAlarmItems.size
    }
}

interface AlarmItemListenerInterface {
    fun onCheckboxClick(position: Int, item: AlarmItem)
    fun onItemLongClick(position: Int, item: AlarmItem)
}
