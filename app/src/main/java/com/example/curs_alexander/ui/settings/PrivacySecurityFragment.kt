package com.example.curs_alexander.ui.settings

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.curs_alexander.R
import com.example.curs_alexander.data.AppDataCleaner
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Экран "Безопасность и конфиденциальность данных".
 */
class PrivacySecurityFragment : Fragment() {

    private var isDeleting: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_privacy_security, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnExport = view.findViewById<MaterialButton>(R.id.btnPrivacyExport)
        val btnDeleteAll = view.findViewById<MaterialButton>(R.id.btnPrivacyDeleteAll)
        val progressDelete = view.findViewById<ProgressBar>(R.id.progressPrivacyDelete)

        fun renderDeleting() {
            btnDeleteAll.isEnabled = !isDeleting
            btnExport.isEnabled = !isDeleting
            progressDelete.visibility = if (isDeleting) View.VISIBLE else View.GONE
        }

        btnExport.setOnClickListener {
            if (isDeleting) return@setOnClickListener
            // Переиспользуем существующий экран PDF-экспорта
            findNavController().navigate(R.id.medicalCardFragment)
        }

        btnDeleteAll.setOnClickListener {
            if (isDeleting) return@setOnClickListener
            // Передаём колбэк, а не результат вызова
            confirmDeleteAllData(onConfirm = { deleteAllData(::renderDeleting) })
        }

        renderDeleting()
    }

    private fun confirmDeleteAllData(onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.privacy_delete_all_title)
            .setMessage(R.string.privacy_delete_all_message)
            .setPositiveButton(R.string.privacy_delete_all_confirm) { _: DialogInterface, _: Int ->
                onConfirm()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun deleteAllData(renderDeleting: () -> Unit) {
        isDeleting = true
        renderDeleting()

        viewLifecycleOwner.lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { AppDataCleaner.clearAll(requireContext()) }.isSuccess
            }

            isDeleting = false
            renderDeleting()

            if (ok) {
                Toast.makeText(requireContext(), R.string.privacy_delete_all_done, Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), R.string.privacy_delete_all_failed, Toast.LENGTH_LONG).show()
            }
        }
    }
}
