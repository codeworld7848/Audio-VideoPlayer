package com.anand.di

import com.anand.inventory.data.repository.InventoryRepositoryImpl
import com.anand.inventory.domain.repository.InventoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    @Provides
    @Singleton
    fun provideInventoryRepository(): InventoryRepository = InventoryRepositoryImpl()
}