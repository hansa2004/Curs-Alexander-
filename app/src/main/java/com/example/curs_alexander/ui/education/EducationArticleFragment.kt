package com.example.curs_alexander.ui.education

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.curs_alexander.R
import com.google.android.material.textview.MaterialTextView

class EducationArticleFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_education_article, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = requireArguments().getString(ARG_ARTICLE_ID)
        val article = id?.let { EducationRepository.getById(it) }
            ?: EducationRepository.getAll().first()

        view.findViewById<MaterialTextView>(R.id.tvEducationArticleTitle)
            .setText(article.titleRes)

        val body = getString(article.bodyRes)
        val disclaimer = getString(R.string.education_disclaimer)
        view.findViewById<MaterialTextView>(R.id.tvEducationArticleBody).text = body + disclaimer
    }

    companion object {
        const val ARG_ARTICLE_ID = "articleId"
    }
}
