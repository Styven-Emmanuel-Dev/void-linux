package com.voidlinux.feature.linux

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.voidlinux.core.designsystem.Components
import com.voidlinux.feature.linux.databinding.FragmentLinuxBinding

class LinuxFragment : Fragment() {

    private var _binding: FragmentLinuxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LinuxViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLinuxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDistroSpinner()
        setupButtons()
        observeState()
    }

    private fun setupDistroSpinner() {
        val names = DistroCatalog.all.map { it.displayName }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            names
        )
        binding.distroSpinner.adapter = adapter

        binding.distroSpinner.setSelection(
            DistroCatalog.all.indexOfFirst { it.id == viewModel.uiState.value.distroId }
                .coerceAtLeast(0)
        )
    }

    private fun setupButtons() {
        binding.installButton.setOnClickListener {
            val idx = binding.distroSpinner.selectedItemPosition
            val distro = DistroCatalog.all.getOrNull(idx) ?: return@setOnClickListener
            viewModel.selectDistro(distro.id)
            viewModel.install()
        }

        binding.uninstallButton.setOnClickListener {
            viewModel.uninstall()
        }
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.statusText.text = state.statusMessage
            binding.progressBar.visibility =
                if (state.installing) View.VISIBLE else View.GONE
            binding.progressBar.progress = state.progress

            binding.installButton.isEnabled =
                !state.installing && !state.installed && state.nativeReady
            binding.uninstallButton.isEnabled =
                !state.installing && state.installed

            binding.nativeWarning.visibility =
                if (state.nativeReady) View.GONE else View.VISIBLE

            state.errorMessage?.let { msg ->
                Components.showSnackLong(binding.root, msg)
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}