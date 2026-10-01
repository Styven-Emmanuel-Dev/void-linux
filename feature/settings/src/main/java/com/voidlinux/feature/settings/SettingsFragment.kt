package com.voidlinux.feature.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.voidlinux.core.designsystem.Components
import com.voidlinux.feature.settings.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.openPermissions.setOnClickListener {
            viewModel.openAllPermissions()
        }

        binding.refresh.setOnClickListener {
            viewModel.refresh()
        }

        observeState()
    }

    private fun observeState() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            renderPermissions(state.permissions)
            renderSteps(state.steps)

            state.errorMessage?.let {
                Components.showSnackLong(binding.root, it)
                viewModel.clearMessages()
            }
        }
    }

    private fun renderPermissions(list: List<PermissionManager.PermissionStatus>) {
        binding.permissionsContainer.removeAllViews()

        list.forEach { perm ->
            val tv = TextView(requireContext()).apply {
                text = "${if (perm.granted) "✅" else "❌"}  ${perm.name}"
                setTextColor(
                    if (perm.granted)
                        ContextCompat.getColor(requireContext(), R.color.void_accent)
                    else
                        ContextCompat.getColor(requireContext(), R.color.void_error)
                )
                textSize = 14f
                setPadding(0, 12, 0, 12)
            }
            binding.permissionsContainer.addView(tv)
        }
    }

    private fun renderSteps(steps: List<HardeningGuide.HardeningStep>) {
        binding.stepsContainer.removeAllViews()

        steps.forEach { step ->
            val layout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 20, 0, 20)
                isClickable = true
                isFocusable = true
                setOnClickListener { viewModel.runStep(step) }
            }

            val title = TextView(requireContext()).apply {
                text = step.title
                setTextColor(ContextCompat.getColor(requireContext(), R.color.void_text))
                textSize = 15f
            }

            val desc = TextView(requireContext()).apply {
                text = step.description
                setTextColor(ContextCompat.getColor(requireContext(), R.color.void_text_dim))
                textSize = 12f
            }

            layout.addView(title)
            layout.addView(desc)

            binding.stepsContainer.addView(layout)
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