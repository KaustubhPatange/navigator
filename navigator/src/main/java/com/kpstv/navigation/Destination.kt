package com.kpstv.navigation

@JvmInline
value class Destination(val fragments: MutableMap<FragClazz, BaseArgs?> = mutableMapOf()) {
    companion object {
        fun of(screen: FragClazz): Destination = Destination().apply { fragments[screen] = null }
        fun of(screen: Pair<FragClazz, BaseArgs>): Destination = Destination().apply { fragments[screen.first] = screen.second }
        fun of(screens: List<FragClazz>): Destination = Destination().apply { screens.forEach { frag -> fragments[frag] = null } }
    }
}