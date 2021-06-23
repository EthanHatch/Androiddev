package org.jdc.template.ux.directory

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import org.jdc.template.model.db.main.directoryitem.DirectoryItem
import org.jdc.template.model.repository.IndividualRepository
import org.jdc.template.util.coroutine.channel.ViewModelChannel
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel
@Inject constructor(
    private val individualRepository: IndividualRepository
) : ViewModel() {
    private val _eventChannel: ViewModelChannel<Event> = ViewModelChannel(this)
    val eventChannel: ReceiveChannel<Event> = _eventChannel

    val directoryListFlow: Flow<List<DirectoryItem>> // change to ShareFlow.shareIn(viewModelScope) when available (in coroutine library https://github.com/Kotlin/kotlinx.coroutines/issues/2069)
        get() = individualRepository.getDirectoryListFlow()

    fun addIndividual() {
        _eventChannel.sendAsync(Event.Navigate(DirectoryFragmentDirections.actionToIndividualEditFragment()))
    }

    fun onDirectoryIndividualClicked(directoryListItem: DirectoryItem) {
        _eventChannel.sendAsync(Event.Navigate(DirectoryFragmentDirections.actionToIndividualFragment(directoryListItem.id)))
    }

    sealed class Event {
        class Navigate(val direction: NavDirections) : Event()
    }
}