package com.voidlinux.feature.terminal

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.voidlinux.core.designsystem.Components
import com.voidlinux.feature.terminal.databinding.FragmentTerminalBinding
import kotlinx.coroutines.launch

class TerminalFragment : Fragment() {

    private var _binding: FragmentTerminalBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TerminalViewModel by viewModels()
    private lateinit var buffer: TerminalBuffer

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTerminalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buffer = TerminalBuffer()
        binding.terminalView.buffer = buffer

        binding.terminalView.onInput = { data -> viewModel.writeInput(data) }
        binding.terminalView.onResize = { cols, rows -> viewModel.resize(cols, rows) }

        binding.terminalView.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                val ansi = KeyboardHandler.keyCodeToAnsi(keyCode, event)
                if (ansi != null) {
                    viewModel.writeInput(ansi)
                    return@setOnKeyListener true
                }
            }
            false
        }
        binding.terminalView.requestFocus()

        binding.keyEsc.setOnClickListener { viewModel.writeInput("\u001B") }
        binding.keyTab.setOnClickListener { viewModel.writeInput("\t") }
        binding.keyCtrl.setOnClickListener { viewModel.writeInput("\u0003") }
        binding.keyUp.setOnClickListener { viewModel.writeInput("\u001B[A") }
        binding.keyDown.setOnClickListener { viewModel.writeInput("\u001B[B") }
        binding.keyLeft.setOnClickListener { viewModel.writeInput("\u001B[D") }
        binding.keyRight.setOnClickListener { viewModel.writeInput("\u001B[C") }

        binding.keyStart.setOnClickListener {
            viewModel.startSession(buffer) { text ->
                activity?.runOnUiThread { binding.terminalView.writeText(text) }
            }
        }
        binding.keyStop.setOnClickListener { viewModel.stopSession() }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                if (!state.linuxReady) {
                    binding.terminalView.writeText(
                        "\r\n[Void-Linux] Terminal prêt.\r\n"
                    )
                }
                state.errorMessage?.let { msg ->
                    Components.showSnackLong(binding.root, msg)
                    viewModel.clearError()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopSession()
        _binding = null
    }
}
