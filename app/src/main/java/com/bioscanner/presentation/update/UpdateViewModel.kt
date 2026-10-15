package com.bioscanner.presentation.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bioscanner.BuildConfig
import com.bioscanner.domain.update.AppUpdate
import com.bioscanner.domain.update.UpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UpdateViewModel(
    private val updateChecker: UpdateChecker,
) : ViewModel() {

    private val _update = MutableStateFlow<AppUpdate?>(null)
    val update: StateFlow<AppUpdate?> = _update.asStateFlow()

    // Uma verificação por abertura do app (o ViewModel da Activity sobrevive à rotação).
    init {
        viewModelScope.launch { _update.value = updateChecker.findUpdate(BuildConfig.VERSION_NAME) }
    }

    fun dismiss() {
        _update.value = null
    }
}
