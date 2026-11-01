package com.movies.cinetrack.di

import com.movies.cinetrack.BuildConfig
import com.movies.cinetrack.core.diagnostics.Logger
import com.movies.cinetrack.core.diagnostics.AndroidLogger
import com.movies.cinetrack.data.remote.api.TmdbApi
import com.movies.cinetrack.data.remote.api.ApiRequestExecutor
import com.movies.cinetrack.data.remote.api.TmdbClient
import com.movies.cinetrack.data.repository.TmdbMovieRepository
import com.movies.cinetrack.domain.repository.MovieRepository
import com.movies.cinetrack.domain.usecase.GetMoviesUseCase
import com.movies.cinetrack.domain.usecase.GetMovieDetailsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideLogger(logger: AndroidLogger): Logger = logger

    @Provides
    @Singleton
    fun provideHttpClient(): OkHttpClient =
        TmdbClient.createHttpClient(BuildConfig.TMDB_READ_ACCESS_TOKEN)

    @Provides
    @Singleton
    fun provideTmdbApi(client: OkHttpClient): TmdbApi = TmdbClient.createApi(client)

    @Provides
    @Singleton
    fun provideMovieRepository(api: TmdbApi, executor: ApiRequestExecutor): MovieRepository =
        TmdbMovieRepository(api, executor)

    @Provides
    fun provideGetMoviesUseCase(repository: MovieRepository): GetMoviesUseCase =
        GetMoviesUseCase(repository)

    @Provides
    fun provideGetMovieDetailsUseCase(repository: MovieRepository): GetMovieDetailsUseCase =
        GetMovieDetailsUseCase(repository)
}