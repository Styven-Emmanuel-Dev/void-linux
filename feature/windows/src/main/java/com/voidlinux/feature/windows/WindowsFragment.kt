package com.voidlinux.feature.windows

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.voidlinux.core.designsystem.Components
import com.voidlinux.feature.windows.databinding.FragmentWindowsBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class WindowsFragment : Fragment() {

    private var _binding: FragmentWindowsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WindowsViewModel by viewModels()

    private val pickExe = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri -> copyAndRun(uri) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWindowsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.initWine.setOnClickListener { viewModel.initializeWine() }
        binding.runExe.setOnClickListener { openExePicker() }
        binding.stopExecution.setOnClickListener { viewModel.stopExecution() }
        binding.cleanWine.setOnClickListener { viewModel.cleanWine() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.statusText.text = state.statusMessage
                binding.archText.text = "Architecture : ${state.architecture}"
                binding.outputText.text = state.output

                binding.initWine.isEnabled = !state.wineReady
                binding.runExe.isEnabled = state.box64Available && !state.running
                binding.stopExecution.isEnabled = state.running

                state.errorMessage?.let {
                    Components.showSnackLong(binding.root, it)
                    viewModel.clearError()
                }
            }
        }
    }

    private fun openExePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickExe.launch(intent)
    }

    private fun copyAndRun(uri: Uri) {
        try {
            val name = queryName(uri) ?: "program.exe"
            val target = File(requireContext().cacheDir, name)
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }
            }
            viewModel.runExe(target)
        } catch (e: Exception) {
            Components.showSnackLong(binding.root, "Impossible de lire le fichier")
        }
    }

    private fun queryName(uri: Uri): String? {
        return try {
            requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && idx >= 0) cursor.getString(idx) else null
            }
        } catch (e: Exception) { null }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
