package org.monogram.presentation.auth

import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.update
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.monogram.domain.repository.AuthRepository
import org.monogram.domain.repository.AuthStep
import org.monogram.presentation.root.AppComponentContext
import org.monogram.presentation.util.componentScope

interface AuthComponent {
    val model: Value<Model>

    fun onPhoneEntered(phone: String)
    fun onCodeEntered(code: String)
    fun onResendCode()
    fun onPasswordEntered(password: String)
    fun onBackToPhone()
    fun onProxyClicked()
    fun dismissError()
    fun onReset()

    data class Model(
        val authState: AuthState,
        val isSubmitting: Boolean = false,
        val error: String? = null,
        val phoneNumber: String? = null
    )

    sealed class AuthState {
        object InputPhone : AuthState()
        data class InputCode(
            val codeLength: Int,
            val codeType: String,
            val nextCodeType: String? = null,
            val timeout: Int = 0
        ) : AuthState()
        object InputPassword : AuthState()
    }
}

class DefaultAuthComponent(
    context: AppComponentContext,
    private val onOpenProxy: () -> Unit
) : AuthComponent, AppComponentContext by context {

    private val repository: AuthRepository = container.repositories.authRepository
    private val scope = componentScope

    private val _model = MutableValue(
        AuthComponent.Model(authState = AuthComponent.AuthState.InputPhone)
    )
    override val model: Value<AuthComponent.Model> = _model

    init {
        repository.authState
            .onEach { step ->
                val newAuthState = when (step) {
                    is AuthStep.InputPhone -> AuthComponent.AuthState.InputPhone
                    is AuthStep.InputCode -> AuthComponent.AuthState.InputCode(
                        codeLength = step.codeLength,
                        codeType = step.codeType,
                        nextCodeType = step.nextType,
                        timeout = step.timeout
                    )
                    is AuthStep.InputPassword -> AuthComponent.AuthState.InputPassword
                    else -> null
                }
                if (newAuthState != null) {
                    _model.update {
                        it.copy(
                            authState = newAuthState,
                            isSubmitting = false
                        )
                    }
                }
            }
            .launchIn(scope)

        repository.errors
            .onEach { errorMessage ->
                _model.update {
                    it.copy(
                        error = errorMessage,
                        isSubmitting = false
                    )
                }
            }
            .launchIn(scope)
    }

    override fun onPhoneEntered(phone: String) {
        _model.update { it.copy(isSubmitting = true, phoneNumber = phone) }
        repository.sendPhone(phone)
    }

    override fun onCodeEntered(code: String) {
        _model.update { it.copy(isSubmitting = true) }
        repository.sendCode(code)
    }

    override fun onResendCode() {
        repository.resendCode()
    }

    override fun onPasswordEntered(password: String) {
        _model.update { it.copy(isSubmitting = true) }
        repository.sendPassword(password)
    }

    override fun onBackToPhone() {
        _model.update { it.copy(error = null) }
        repository.reset()
    }

    override fun onProxyClicked() {
        onOpenProxy()
    }

    override fun dismissError() {
        _model.update { it.copy(error = null) }
    }

    override fun onReset() {
        _model.update { it.copy(error = null) }
        repository.reset()
    }
}
