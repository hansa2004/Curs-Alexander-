package com.example.curs_alexander.ui.home

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.setMargins
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.example.curs_alexander.ui.home.quickactions.QuickActionColorPalette
import com.example.curs_alexander.ui.home.quickactions.QuickActionType
import com.example.curs_alexander.ui.home.quickactions.QuickActionsAdapter
import com.example.curs_alexander.ui.home.quickactions.QuickActionsTouchHelper
import com.example.curs_alexander.ui.home.quickactions.QuickActionsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val quickVm: QuickActionsViewModel by viewModels()
    private val tipsVm: HomeTipsViewModel by viewModels()

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

        // Полезная информация (образовательный модуль)
        view.findViewById<View?>(R.id.cardEducation)?.setOnClickListener {
            findNavController().navigate(R.id.educationListFragment)
        }

        // --- Конструктор быстрых действий ---
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerQuickActions)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        lateinit var touchHelper: ItemTouchHelper

        var editMode = false

        val adapter = QuickActionsAdapter(
            onClick = { action ->
                if (action.navArgs != null) {
                    findNavController().navigate(action.navDestinationId, action.navArgs)
                } else {
                    findNavController().navigate(action.navDestinationId)
                }
            },
            onDelete = { pos ->
                quickVm.removeAt(pos)
            },
            onStartDrag = { vh ->
                touchHelper.startDrag(vh)
            },
            onPickColor = { /* legacy */ },
            onPickIcon = { type ->
                if (editMode) {
                    showQuickActionCustomizeDialog(type)
                }
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
            editMode = adapter.editMode
            if (adapter.editMode) {
                tvHint.text = getString(R.string.quick_actions_color_hint)
            } else {
                tvHint.text = getString(R.string.quick_actions_hint)
            }
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
                launch {
                    quickVm.colors.collect { colors ->
                        adapter.submitColors(colors)
                    }
                }
                launch {
                    quickVm.iconOverrides.collect { icons ->
                        adapter.submitIconOverrides(icons)
                    }
                }
            }
        }

        // Рендер карточки "Сегодня" + контекстных подсказок
        val tvTodayContent = view.findViewById<TextView>(R.id.tvTodayContent)
        val btnTodayAction = view.findViewById<MaterialButton>(R.id.btnTodayAction)

        val cardHints = view.findViewById<MaterialCardView>(R.id.cardContextHints)
        val hintsContainer = view.findViewById<ViewGroup>(R.id.containerHints)

        fun renderToday(ui: HomeTipsViewModel.TodayCardUi) {
            tvTodayContent.text = ui.message
            val hasAction = ui.actionText != null && ui.actionDestinationId != null
            if (hasAction) {
                btnTodayAction.text = ui.actionText
                btnTodayAction.visibility = View.VISIBLE
                btnTodayAction.setOnClickListener {
                    findNavController().navigate(ui.actionDestinationId)
                }
            } else {
                btnTodayAction.visibility = View.GONE
                btnTodayAction.setOnClickListener(null)
            }
        }

        fun renderHints(list: List<HomeTipsViewModel.HintUi>) {
            hintsContainer.removeAllViews()
            if (list.isEmpty()) {
                cardHints.visibility = View.GONE
                return
            }
            cardHints.visibility = View.VISIBLE

            val inflater = LayoutInflater.from(hintsContainer.context)
            list.forEach { hint ->
                val row = inflater.inflate(R.layout.item_home_hint, hintsContainer, false)
                val tv = row.findViewById<TextView>(R.id.tvHintText)
                val btn = row.findViewById<MaterialButton>(R.id.btnHintAction)

                tv.text = hint.message
                val hasAction = hint.actionText != null && hint.actionDestinationId != null
                if (hasAction) {
                    btn.text = hint.actionText
                    btn.visibility = View.VISIBLE
                    btn.setOnClickListener { findNavController().navigate(hint.actionDestinationId) }
                } else {
                    btn.visibility = View.GONE
                    btn.setOnClickListener(null)
                }

                hintsContainer.addView(row)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    tipsVm.todayCard.collect { renderToday(it) }
                }
                launch {
                    tipsVm.hints.collect { renderHints(it) }
                }
            }
        }
    }

    private fun showAddQuickActionDialog(
        existing: Set<QuickActionType>,
        onAdd: (QuickActionType) -> Unit
    ) {
        val all = QuickActionType.entries.toList()
        val available = all
            .filter { it !in existing }
            .sortedWith(compareByDescending<QuickActionType> { it.isShortcutToSection() }
                .thenBy { getString(it.titleRes) })

        if (available.isEmpty()) return

        val dialogView = layoutInflater.inflate(R.layout.dialog_quick_actions_add, null)
        val recycler = dialogView.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())

        lateinit var dialog: AlertDialog

        val adapter = com.example.curs_alexander.ui.home.quickactions.QuickActionPickerAdapter { type ->
            onAdd(type)
            dialog.dismiss()
        }
        recycler.adapter = adapter
        adapter.submit(available, quickVm.iconOverrides.value)

        dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.home_quick_actions_title))
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.show()
    }

    private fun QuickActionType.isShortcutToSection(): Boolean {
        return when (this) {
            QuickActionType.OPEN_ANALYTICS,
            QuickActionType.OPEN_DATA_STATUS,
            QuickActionType.OPEN_MEDICAL_CARD,
            QuickActionType.OPEN_USER_PARAMS,
            QuickActionType.OPEN_SETTINGS,
            QuickActionType.OPEN_EDUCATION -> true

            else -> false
        }
    }

    // В режиме редактирования тап по плитке открывает настройку (иконка/цвет)
    private fun showQuickActionCustomizeDialog(type: QuickActionType) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_quick_action_customize, null)

        val recyclerIcons = dialogView.findViewById<RecyclerView>(R.id.recyclerIcons)
        recyclerIcons.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            requireContext(),
            RecyclerView.HORIZONTAL,
            false
        )
        recyclerIcons.adapter = com.example.curs_alexander.ui.home.quickactions.QuickActionIconOptionsAdapter { style ->
            quickVm.setIcon(type, style)
        }

        val grid = dialogView.findViewById<android.widget.GridLayout>(R.id.gridColors)
        val btnResetColor = dialogView.findViewById<MaterialButton>(R.id.btnResetColor)
        val btnResetIcon = dialogView.findViewById<MaterialButton>(R.id.btnResetIcon)

        val sizePx = resources.displayMetrics.density * 36
        val marginPx = (resources.displayMetrics.density * 6).toInt()

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.quick_actions_customize_title))
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        grid.removeAllViews()
        QuickActionColorPalette.colors.forEach { colorInt ->
            val dot = ImageView(requireContext()).apply {
                // qa_color_dot — это shape (oval) с прозрачной заливкой, поэтому его нужно тинтить как background
                setBackgroundResource(R.drawable.qa_color_dot)
                backgroundTintList = ColorStateList.valueOf(colorInt)

                val lp = android.widget.GridLayout.LayoutParams().apply {
                    width = sizePx.toInt()
                    height = sizePx.toInt()
                    setMargins(marginPx)
                }
                layoutParams = lp
                isClickable = true
                isFocusable = true
                setOnClickListener { quickVm.setColor(type, colorInt) }
            }
            grid.addView(dot)
        }

        btnResetColor.setOnClickListener { quickVm.clearColor(type) }
        btnResetIcon.setOnClickListener { quickVm.clearIcon(type) }

        dialog.show()
    }
}
