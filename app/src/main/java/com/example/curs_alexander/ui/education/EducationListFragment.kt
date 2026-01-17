package com.example.curs_alexander.ui.education

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.curs_alexander.R
import com.google.android.material.card.MaterialCardView

class EducationListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_education_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.tvEducationTitle).text =
            getString(R.string.education_screen_title)

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerEducation)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = EducationAdapter(
            items = EducationRepository.getAll(),
            onClick = { article ->
                findNavController().navigate(
                    R.id.educationArticleFragment,
                    bundleOf(EducationArticleFragment.ARG_ARTICLE_ID to article.id)
                )
            }
        )
    }

    private class EducationAdapter(
        private val items: List<EducationArticle>,
        private val onClick: (EducationArticle) -> Unit
    ) : RecyclerView.Adapter<EducationAdapter.VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_education_article, parent, false)
            return VH(v, onClick)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        override fun getItemCount(): Int = items.size

        class VH(itemView: View, private val onClick: (EducationArticle) -> Unit) : RecyclerView.ViewHolder(itemView) {
            private val card: MaterialCardView = itemView.findViewById(R.id.cardEducation)
            private val tvTitle: TextView = itemView.findViewById(R.id.tvEducationItemTitle)

            fun bind(item: EducationArticle) {
                tvTitle.setText(item.titleRes)
                card.setOnClickListener { onClick(item) }
            }
        }
    }
}

