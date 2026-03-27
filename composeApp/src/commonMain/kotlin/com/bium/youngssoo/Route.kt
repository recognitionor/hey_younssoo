package com.bium.youngssoo

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Community : Route

    @Serializable
    data class Level(val id: String) : Route
    @Serializable
    data class MyInfo(val id: String) : Route
}