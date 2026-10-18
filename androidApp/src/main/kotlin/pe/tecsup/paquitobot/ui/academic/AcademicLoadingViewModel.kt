package pe.tecsup.paquitobot.ui.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Mecanica comun de los 4 ViewModels academicos, para que se comporten igual:
 *
 * - [load] reemplaza cualquier carga en curso (reintentos seguidos no se
 *   pisan entre si).
 * - Si la carga FALLA, [errorMessage] pasa a un mensaje generico y [uiState]
 *   conserva los ultimos datos buenos (nunca se pisa con datos vacios).
 * - Si la carga sale bien, [uiState] se actualiza y [errorMessage] se limpia.
 *
 * Las subclases deben llamar a `load()` en su propio `init` (despues de
 * inicializar sus dependencias).
 */
abstract class AcademicLoadingViewModel<T>(initial: T) : ViewModel() {

    private val _uiState = MutableStateFlow(initial)
    val uiState: StateFlow<T> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var loadJob: Job? = null

    /** Arma el nuevo estado a partir del actual; un [Result] fallido se muestra como error. */
    protected abstract suspend fun fetch(current: T): Result<T>

    fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            fetch(_uiState.value)
                .onSuccess { data ->
                    _uiState.value = data
                    _errorMessage.value = null
                }
                .onFailure { _errorMessage.value = ACADEMIC_LOAD_ERROR }
        }
    }
}
