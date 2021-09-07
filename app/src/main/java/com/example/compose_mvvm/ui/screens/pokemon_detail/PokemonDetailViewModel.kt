package com.example.compose_mvvm.ui.screens.pokemon_detail

import androidx.lifecycle.ViewModel
import com.example.compose_mvvm.data.remote.responses.Pokemon
import com.example.compose_mvvm.repositories.PokemonRepository
import com.example.compose_mvvm.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(private val repository: PokemonRepository): ViewModel() {
    suspend fun getPokemonInfo(pokemonName: String): Resource<Pokemon> {
        return repository.getPokemonInfo(pokemonName)
    }
}