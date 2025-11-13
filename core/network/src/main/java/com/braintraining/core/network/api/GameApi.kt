package com.braintraining.core.network.api

import com.braintraining.core.model.Game
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface GameApi {
    @GET("games")
    suspend fun getGames(): List<Game>

    @GET("games/{id}")
    suspend fun getGameById(@Path("id") id: String): Game

    @PUT("games/{id}")
    suspend fun updateGame(@Path("id") id: String, @Body game: Game): Game

    @DELETE("games/{id}")
    suspend fun deleteGame(@Path("id") id: String)
}