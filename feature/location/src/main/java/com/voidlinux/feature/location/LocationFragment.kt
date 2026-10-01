package com.voidlinux.feature.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.voidlinux.core.designsystem.Components
import com.voidlinux.feature.location.databinding.FragmentLocationBinding

class LocationFragment : Fragment() {

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LocationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val presets = MockLocationProvider.PRESETS.keys.toList()
        binding.presetSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            presets
        )

        binding.presetSpinner.setSelection(
            presets.indexOf(viewModel.uiState.value.presetName).coerceAtLeast(0)
        )

        binding.applyPreset.setOnClickListener {
            val name = binding.presetSpinner.selectedItem as? String ?: return@setOnClickListener
            viewModel.applyPreset(name)
        }

        binding.toggleMock.setOnClickListener {
            viewModel.toggleMock()
        }

        binding.customApply.setOnClickListener {
            val lat = binding.latInput.text.toString().toDoubleOrNull()
            val lon = binding.lonInput.text.toString().toDoubleOrNull()
            if (lat == null || lon == null) {
                Components.showSnackLong(binding.root, "Coordonnées invalides")
                return@setOnClickListener
            }
            viewModel.setCustomLocation(lat, lon)
        }

        binding.openSettings.setOnClickListener { viewModel.openLocationSettings() }
        binding.openDevOptions.setOnClickListener { viewModel.openMockSettings() }

        observeState()
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.statusText.text = state.statusMessage
            binding.toggleMock.text =
                if (state.active) "Désactiver la fausse position"
                else "Activer la fausse position"

            binding.coordsText.text = "%.6f, %.6f".format(state.currentLat, state.currentLon)

            binding.rootedWarning.visibility =
                if (state.deviceRooted) View.VISIBLE else View.GONE

            state.errorMessage?.let {
                Components.showSnackLong(binding.root, it)
                viewModel.clearError()
            }
        }
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