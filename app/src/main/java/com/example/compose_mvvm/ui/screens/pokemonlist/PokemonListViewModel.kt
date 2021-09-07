package com.example.compose_mvvm.ui.screens.pokemonlist

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.compose_mvvm.data.models.PokedexListEntry
import com.example.compose_mvvm.repositories.PokemonRepository
import com.example.compose_mvvm.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(private val pokemonRepository: PokemonRepository): ViewModel() {
    private var curPage = 0
    var pokemonList = mutableStateOf<List<PokedexListEntry?>>(listOf())
    var loadError = mutableStateOf("")
    var isLoading = mutableStateOf(false)
    var endReaches = mutableStateOf(false)

    private var cachedPokemonList = listOf<PokedexListEntry?>()
    private var isSearchStarting = true
    var isSearching = mutableStateOf(false)

    init {
        loadPokemonPaginated()
    }

    fun searchPokemonList(query: String) {
        val listToSearch = if (isSearchStarting) {
            pokemonList.value
        } else {
            cachedPokemonList
        }

        viewModelScope.launch(Dispatchers.Default) {
            if (query.isEmpty()) {
                pokemonList.value = cachedPokemonList
                isSearching.value = false
                isSearchStarting = true
                return@launch
            }

            val results = listToSearch.filter { entry ->
                entry?.pokemonName.toString().contains(query.trim(), ignoreCase = true) || entry?.number.toString() == query.trim()
            }

            if (isSearchStarting) {
                cachedPokemonList = pokemonList.value
                isSearchStarting = false
            }
            pokemonList.value = results
            isSearching.value = true
        }
    }

    fun loadPokemonPaginated() {
        Timber.d("curPage: $curPage")
        viewModelScope.launch {
            isLoading.value = true
            val result = pokemonRepository.getPokemonList(20, curPage * 20)
            when(result) {
                is Resource.Success -> {
                    result.data?.let { data ->
                        Timber.d("data: ${data.count}")
                        endReaches.value = curPage * 20 >= data.count ?: 0
                        val pokedexEntries = data.results?.mapIndexed { index, entry ->
                            entry.url?.let { entryUrl ->
                                val number = if (entryUrl.endsWith("/")) {
                                    entryUrl.dropLast(1).takeLastWhile { it.isDigit() }
                                } else {
                                    entryUrl.takeLastWhile { it.isDigit() }
                                }

                                val url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${number}.png"
                                PokedexListEntry(entry.name?.capitalize(Locale.ROOT).toString(), url, number.toInt())
                            }
                        }
                        curPage++

                        loadError.value = ""
                        isLoading.value = false
                        pokedexEntries?.let {
                            pokemonList.value += it
                        }
                    }
                }

                is Resource.Error -> {
                    loadError.value = result.message.toString()
                    isLoading.value = false
                }
            }
        }
    }

    fun calcDominantColor(bitmap: Bitmap, onFinish: (Color) -> Unit) {
        val bmp = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinish(Color(colorValue))
                bmp.recycle()
            }
        }
    }
}