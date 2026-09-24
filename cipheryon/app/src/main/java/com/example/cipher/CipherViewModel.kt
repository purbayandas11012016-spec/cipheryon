package com.example.cipher

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CipherUiState(
    val inputText: String = "",
    val selectedMode: CipherMode = CipherMode.HEXADECIMAL,
    val outputText: String = "",
    val isError: Boolean = false,
    val statusMessage: String? = null
)

class CipherViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CipherUiState())
    val uiState: StateFlow<CipherUiState> = _uiState.asStateFlow()

    fun onInputChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun onModeChanged(mode: CipherMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun encrypt() {
        val currentInput = _uiState.value.inputText
        val mode = _uiState.value.selectedMode
        val result = CipherEngine.encrypt(currentInput, mode)
        val isError = result.startsWith("Error:")
        _uiState.update {
            it.copy(
                outputText = result,
                isError = isError,
                statusMessage = null
            )
        }
    }

    fun decrypt() {
        val currentInput = _uiState.value.inputText
        val mode = _uiState.value.selectedMode
        val result = CipherEngine.decrypt(currentInput, mode)
        val isError = result.startsWith("Error:")
        _uiState.update {
            it.copy(
                outputText = result,
                isError = isError,
                statusMessage = null
            )
        }
    }

    fun swapInputOutput() {
        val currentOutput = _uiState.value.outputText
        if (currentOutput.isNotEmpty() && !_uiState.value.isError) {
            _uiState.update {
                it.copy(
                    inputText = currentOutput,
                    outputText = "",
                    statusMessage = "Output moved to input!"
                )
            }
        }
    }

    fun clearAll() {
        _uiState.update {
            it.copy(
                inputText = "",
                outputText = "",
                isError = false,
                statusMessage = null
            )
        }
    }

    fun loadSample(text: String) {
        _uiState.update {
            it.copy(
                inputText = text,
                statusMessage = "Loaded sample text"
            )
        }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }
}
