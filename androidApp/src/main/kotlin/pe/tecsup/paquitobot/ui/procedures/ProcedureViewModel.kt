package pe.tecsup.paquitobot.ui.procedures

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.domain.procedures.ProcedureCatalog
import pe.tecsup.paquitobot.domain.procedures.ProcedureRepository

sealed interface ProcedureUiState {
    data object Loading : ProcedureUiState
    data class Ready(val catalog: ProcedureCatalog) : ProcedureUiState
    data object Error : ProcedureUiState
}
class ProcedureViewModel(private val repository: ProcedureRepository) : ViewModel() {
    private val state = MutableStateFlow<ProcedureUiState>(ProcedureUiState.Loading)
    val uiState = state.asStateFlow()
    private var job: Job? = null
    init { load() }
    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            state.value = ProcedureUiState.Loading
            state.value = repository.catalog().fold({ ProcedureUiState.Ready(it) }, { ProcedureUiState.Error })
        }
    }
}
