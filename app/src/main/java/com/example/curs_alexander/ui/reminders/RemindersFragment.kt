package com.example.curs_alexander.ui.reminders

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.data.reminders.ReminderEntity
import com.example.curs_alexander.data.reminders.ReminderType
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch
import java.util.Calendar

class RemindersFragment : Fragment() {

    companion object {
        const val ARG_OPEN_ADD_DIALOG = "openAddDialog"
    }

    private val viewModel: RemindersViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_reminders, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerReminders)
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnAddReminder)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        val adapter = RemindersAdapter(
            onToggle = { item, enabled -> viewModel.setEnabled(item, enabled) },
            onDelete = { item -> viewModel.delete(item) }
        )
        recycler.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reminders.collect { list ->
                    adapter.submit(list)
                }
            }
        }

        btnAdd.setOnClickListener {
            showAddDialog()
        }

        // Если пришли из quick action — сразу открываем добавление
        if (arguments?.getBoolean(ARG_OPEN_ADD_DIALOG, false) == true) {
            // Постим в очередь, чтобы точно после отрисовки/attach
            view.post { showAddDialog() }
        }
    }

    private fun showAddDialog() {
        val items = arrayOf(
            getString(R.string.reminder_type_bp),
            getString(R.string.reminder_type_symptom)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.reminder_add_title))
            .setItems(items) { _, which ->
                val type = if (which == 0) ReminderType.BLOOD_PRESSURE else ReminderType.SYMPTOM
                pickTime { hour, minute ->
                    viewModel.add(type, hour, minute, enabled = true)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun pickTime(onPicked: (Int, Int) -> Unit) {
        val now = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute -> onPicked(hourOfDay, minute) },
            now.get(Calendar.HOUR_OF_DAY),
            now.get(Calendar.MINUTE),
            true
        ).show()
    }

    private class RemindersAdapter(
        private val onToggle: (ReminderEntity, Boolean) -> Unit,
        private val onDelete: (ReminderEntity) -> Unit
    ) : RecyclerView.Adapter<RemindersAdapter.VH>() {

        private val items = mutableListOf<ReminderEntity>()

        fun submit(newItems: List<ReminderEntity>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reminder, parent, false)
            return VH(v, onToggle, onDelete)
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        class VH(
            itemView: View,
            private val onToggle: (ReminderEntity, Boolean) -> Unit,
            private val onDelete: (ReminderEntity) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {

            private val tvTitle: TextView = itemView.findViewById(R.id.tvReminderTitle)
            private val tvTime: TextView = itemView.findViewById(R.id.tvReminderTime)
            private val sw: MaterialSwitch = itemView.findViewById(R.id.switchEnabled)
            private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

            fun bind(item: ReminderEntity) {
                tvTitle.text = when (item.type) {
                    ReminderType.BLOOD_PRESSURE -> itemView.context.getString(R.string.reminder_type_bp)
                    ReminderType.SYMPTOM -> itemView.context.getString(R.string.reminder_type_symptom)
                }
                tvTime.text = String.format("%02d:%02d", item.hour, item.minute)

                sw.setOnCheckedChangeListener(null)
                sw.isChecked = item.enabled
                sw.setOnCheckedChangeListener { _, isChecked ->
                    onToggle(item, isChecked)
                }

                btnDelete.setOnClickListener { onDelete(item) }
            }
        }
    }
}
