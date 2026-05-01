package es.guardianos.senior.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted

class MessageViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = SeniorDatabase.getInstance(application).messageDao()

    val messages: StateFlow<List<MessageEntity>> = dao.getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
