package com.voidlinux.feature.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.voidlinux.core.designsystem.Components
import com.voidlinux.feature.security.databinding.FragmentSecurityBinding

class SecurityFragment : Fragment() {

    private var _binding: FragmentSecurityBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SecurityViewModel by viewModels()
    private lateinit var adapter: SecurityEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecurityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SecurityEventAdapter()
        binding.eventsList.layoutManager = LinearLayoutManager(requireContext())
        binding.eventsList.adapter = adapter

        binding.toggleMonitoring.setOnClickListener { viewModel.toggleMonitoring() }
        binding.scanNow.setOnClickListener { viewModel.runManualScan() }
        binding.clearEvents.setOnClickListener { viewModel.clearEvents() }

        observeState()
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            binding.statusText.text = state.statusMessage
            binding.toggleMonitoring.text =
                if (state.monitoring) "Arrêter la surveillance"
                else "Démarrer la surveillance"

            adapter.submit(state.events)

            state.errorMessage?.let {
                Components.showSnackLong(binding.root, it)
                viewModel.clearError()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
