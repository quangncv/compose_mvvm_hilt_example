package com.example.compose_mvvm.repositories

import com.example.compose_mvvm.data.remote.PokeApi
import com.example.compose_mvvm.data.remote.responses.Pokemon
import com.example.compose_mvvm.data.remote.responses.PokemonList
import com.example.compose_mvvm.utils.Resource
import dagger.hilt.android.scopes.ActivityScoped
import java.lang.Exception
import javax.inject.Inject

@ActivityScoped
class PokemonRepository @Inject constructor(
    private val api: PokeApi
){
    suspend fun getPokemonList(limit: Int, offset: Int): Resource<PokemonList> {
        val response = try {
            api.getPokemonList(limit, offset)
        } catch (ex: Exception) {
            return Resource.Error("An unknown error occured")
        }

        return Resource.Success(response)
    }

    suspend fun getPokemonInfo(name: String): Resource<Pokemon> {
        val response = try {
            api.getPokemonInfo(name = name)
        } catch (ex: Exception) {
            return Resource.Error("An unknown error occured")
        }

        return Resource.Success(response)
    }
}