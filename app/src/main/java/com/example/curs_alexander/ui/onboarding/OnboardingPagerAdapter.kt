package com.example.curs_alexander.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.google.android.material.textfield.TextInputEditText
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R

class OnboardingPagerAdapter(
    private val pages: List<Int>,
    private val onNextClick: (Int) -> Unit,
    private val onFinishClick: (String?, Boolean) -> Unit
) : RecyclerView.Adapter<OnboardingPagerAdapter.PageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return PageViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int = pages[position]

    override fun getItemCount(): Int = pages.size

    override fun onBindViewHolder(holder: PageViewHolder, position: Int) {
        val itemView = holder.itemView
        when (position) {
            0 -> itemView.findViewById<Button>(R.id.btnNext1)?.setOnClickListener { onNextClick(position) }
            1 -> itemView.findViewById<Button>(R.id.btnNext2)?.setOnClickListener { onNextClick(position) }
            2 -> itemView.findViewById<Button>(R.id.btnNext3)?.setOnClickListener { onNextClick(position) }
            3 -> itemView.findViewById<Button>(R.id.btnNext4)?.setOnClickListener { onNextClick(position) }
            4 -> itemView.findViewById<Button>(R.id.btnNext5)?.setOnClickListener { onNextClick(position) }
            5 -> itemView.findViewById<Button>(R.id.btnNext6)?.setOnClickListener { onNextClick(position) }
            6 -> {
                val consent = itemView.findViewById<CheckBox>(R.id.cbConsentLast)
                val startBtn = itemView.findViewById<Button>(R.id.btnStartLast)
                val nameEdit = itemView.findViewById<TextInputEditText>(R.id.etUserNameLast)
                if (startBtn != null && consent != null) {
                    startBtn.isEnabled = consent.isChecked
                    consent.setOnCheckedChangeListener { _, isChecked ->
                        startBtn.isEnabled = isChecked
                    }
                    startBtn.setOnClickListener {
                        val name = nameEdit?.text?.toString()?.trim()
                        onFinishClick(name, consent.isChecked)
                    }
                }
            }
        }
    }

    class PageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
