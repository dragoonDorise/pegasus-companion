package com.pegasus.artwork.di

import com.pegasus.artwork.data.repository.ArtworkRepositoryImpl
import com.pegasus.artwork.data.repository.RomScannerRepositoryImpl
import com.pegasus.artwork.data.repository.ScreenScraperRepositoryImpl
import com.pegasus.artwork.data.repository.ThemesRepositoryImpl
import com.pegasus.artwork.domain.repository.ArtworkRepository
import com.pegasus.artwork.domain.repository.RomScannerRepository
import com.pegasus.artwork.domain.repository.ScreenScraperRepository
import com.pegasus.artwork.domain.repository.ThemesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRomScannerRepository(impl: RomScannerRepositoryImpl): RomScannerRepository

    @Binds
    @Singleton
    abstract fun bindScreenScraperRepository(impl: ScreenScraperRepositoryImpl): ScreenScraperRepository

    @Binds
    @Singleton
    abstract fun bindArtworkRepository(impl: ArtworkRepositoryImpl): ArtworkRepository

    @Binds
    @Singleton
    abstract fun bindThemesRepository(impl: ThemesRepositoryImpl): ThemesRepository
}
