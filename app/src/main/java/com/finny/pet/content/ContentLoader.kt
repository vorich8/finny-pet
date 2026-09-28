package com.finny.pet.content

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ContentLoader @Inject constructor(@ApplicationContext private val context: Context) {
    fun load(name: String): String = context.assets.open(name).bufferedReader().use { it.readText() }
    fun lessons() = load("lessons.json")
    fun games() = load("games.json")
    fun shop() = load("shop.json")
    fun goals() = load("goals.json")
    fun petVariants() = load("pet_variants.json")
}
