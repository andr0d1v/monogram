package org.monogram.presentation.settingsScreens.privacy.userSelection

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import org.monogram.domain.models.UserModel
import org.monogram.domain.repository.UserRepository
import org.monogram.presentation.chatsScreen.currentChat.components.VideoPlayerPool
import org.monogram.presentation.root.AppComponentContext
import kotlinx.coroutines.*
import org.monogram.presentation.util.componentScope

interface UserSelectionComponent {
    val state: Value<State>
    val videoPlayerPool: VideoPlayerPool
    fun onBackClicked()
    fun onSearchQueryChanged(query: String)
    fun onUserClicked(userId: Long)

    data class State(
        val searchQuery: String = "",
        val users: List<UserModel> = emptyList(),
        val isLoading: Boolean = false
    )
}

class DefaultUserSelectionComponent(
    context: AppComponentContext,
    private val onBack: () -> Unit,
    private val onUserSelected: (Long) -> Unit
) : UserSelectionComponent, AppComponentContext by context {

    private val userRepository: UserRepository = container.repositories.userRepository
    override val videoPlayerPool: VideoPlayerPool = container.utils.videoPlayerPool

    private val _state = MutableValue(UserSelectionComponent.State())
    override val state: Value<UserSelectionComponent.State> = _state
    private val scope = componentScope
    private var searchJob: Job? = null

    override fun onBackClicked() {
        onBack()
    }

    override fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _state.update { it.copy(users = emptyList()) }
            return
        }

        searchJob = scope.launch {
            delay(500) // Debounce
            _state.update { it.copy(isLoading = true) }
            try {
                val userId = query.toLongOrNull()
                if (userId != null) {
                    val user = userRepository.getUser(userId)
                    if (user != null) {
                        _state.update { it.copy(users = listOf(user)) }
                    } else {
                        _state.update { it.copy(users = emptyList()) }
                    }
                } else {
                    // TODO: Implement proper search in UserRepository
                    _state.update { it.copy(users = emptyList()) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(users = emptyList()) }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    override fun onUserClicked(userId: Long) {
        onUserSelected(userId)
    }
}