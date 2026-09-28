package com.finny.pet.ui.games

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finny.pet.data.database.ProgressEntity
import com.finny.pet.data.repository.ProgressRepository
import com.finny.pet.domain.usecase.AwardGameResult
import com.finny.pet.domain.GameEconomy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExtendedGameUiState(val gameId:String="",val level:Int=1,val mistakes:Int=0,val hint:String?=null,val finishing:Boolean=false)

@HiltViewModel
class ExtendedGameViewModel @Inject constructor(
    savedStateHandle:SavedStateHandle,
    private val progress:ProgressRepository,
    private val award:AwardGameResult
):ViewModel(){
    private val id:String=savedStateHandle["gameId"]?:"shopping_list"
    private val _state=MutableStateFlow(ExtendedGameUiState(gameId=id))
    val state:StateFlow<ExtendedGameUiState> = _state
    init{viewModelScope.launch{val unlocked=(progress.get("game_level_$id")?.value?:1).coerceIn(1,10);val requested=savedStateHandle.get<Int>("level")?:unlocked;_state.value=_state.value.copy(level=requested.coerceIn(1,unlocked))}}
    fun mistake(message:String){if(!_state.value.finishing)_state.value=_state.value.copy(mistakes=_state.value.mistakes+1,hint=message)}
    fun recordMistake(){if(!_state.value.finishing)_state.value=_state.value.copy(mistakes=_state.value.mistakes+1)}
    fun clearHint(){_state.value=_state.value.copy(hint=null)}
    fun complete(onDone:(Int)->Unit){val s=_state.value;if(s.finishing)return;_state.value=s.copy(finishing=true,hint=null);viewModelScope.launch{val stars=when{ s.mistakes==0->3;s.mistakes<=2->2;else->1};award("${s.gameId}_${s.level}",GameEconomy.rewardForLevel(s.level),stars);val unlocked=(progress.get("game_level_${s.gameId}")?.value?:1).coerceIn(1,10);val next=maxOf(unlocked,(s.level+1).coerceAtMost(10));if(next!=unlocked)progress.save(ProgressEntity("game_level_${s.gameId}",next));onDone(stars)}}
}
