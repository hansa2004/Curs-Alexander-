package com.example.curs_alexander.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.ui.home.quickactions.QuickActionType
import com.example.curs_alexander.ui.home.quickactions.QuickActionsAdapter
import com.example.curs_alexander.ui.home.quickactions.QuickActionsTouchHelper
import com.example.curs_alexander.ui.home.quickactions.QuickActionsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val quickVm: QuickActionsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Карточки разделов
        view.findViewById<MaterialCardView>(R.id.cardMeasurements).setOnClickListener {
            findNavController().navigate(R.id.healthMeasurementsFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cardSymptoms).setOnClickListener {
            findNavController().navigate(R.id.symptomsListFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cardReminders).setOnClickListener {
            findNavController().navigate(R.id.remindersFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cardMedicalCard).setOnClickListener {
            findNavController().navigate(R.id.medicalCardFragment)
        }
        view.findViewById<MaterialCardView>(R.id.cardAnalysis).setOnClickListener {
            findNavController().navigate(R.id.analyticsFragment)
        }

        // Шестерёнка под баром
        view.findViewById<MaterialButton>(R.id.btnSettings).setOnClickListener {
            findNavController().navigate(R.id.settingsFragment)
        }

        // --- Конструктор быстрых действий ---
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerQuickActions)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        lateinit var touchHelper: ItemTouchHelper

        val adapter = QuickActionsAdapter(
            onClick = { action ->
                findNavController().navigate(action.navDestinationId)
            },
            onDelete = { pos ->
                quickVm.removeAt(pos)
            },
            onStartDrag = { vh ->
                touchHelper.startDrag(vh)
            }
        )
        recycler.adapter = adapter

        val callback = QuickActionsTouchHelper(
            canMove = { adapter.editMode },
            onMove = { from, to -> quickVm.move(from, to) }
        )
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(recycler)

        // Кнопки управления
        val btnAdd = view.findViewById<MaterialButton>(R.id.btnQuickAdd)
        val btnEdit = view.findViewById<MaterialButton>(R.id.btnQuickEdit)
        val tvHint = view.findViewById<TextView>(R.id.tvQuickActionsHint)

        fun renderEditMode() {
            btnEdit.setText(if (adapter.editMode) R.string.quick_actions_done else R.string.quick_actions_edit)
            tvHint.visibility = if (adapter.editMode) View.VISIBLE else View.GONE
        }

        btnEdit.setOnClickListener {
            adapter.editMode = !adapter.editMode
            renderEditMode()
        }

        // На всякий случай (после пересоздания view)
        renderEditMode()

        btnAdd.setOnClickListener {
            showAddQuickActionDialog(
                existing = quickVm.actions.value.toSet(),
                onAdd = { quickVm.add(it) }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    quickVm.actions.collect { list ->
                        adapter.submit(list)
                    }
                }
            }
        }
    }

    private fun showAddQuickActionDialog(
        existing: Set<QuickActionType>,
        onAdd: (QuickActionType) -> Unit
    ) {
        val all = QuickActionType.values().toList()
        val available = all
            .filter { it != QuickActionType.OPEN_SETTINGS }
            .filter { it !in existing }

        if (available.isEmpty()) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_quick_actions_add, null)
        val listView = dialogView.findViewById<ListView>(R.id.list)

        val labels = available.map { getString(it.titleRes) }
        listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.home_quick_actions_title))
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        listView.setOnItemClickListener { _, _, position, _ ->
            onAdd(available[position])
            dialog.dismiss()
        }

        dialog.show()
    }
}
