package com.anand.inventory.domain.repository

import android.content.Context
import com.anand.core.models.Music

interface InventoryRepository {
    suspend fun getAllLocalMusic(context: Context): List<Music>
}