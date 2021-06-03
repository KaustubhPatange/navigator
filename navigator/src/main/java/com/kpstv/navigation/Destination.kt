package com.kpstv.navigation

import androidx.fragment.app.Fragment

@JvmInline
value class Destination(val fragments: MutableMap<FragClazz, BaseArgs?> = mutableMapOf()) {
    companion object {
        /**
         * Create a destination containing single [Fragment] screen.
         */
        fun of(screen: FragClazz): Destination = Destination().apply { fragments[screen] = null }

        /**
         * Create a destination containing single [Fragment] screen along with additional typed arguments.
         *
         * @see BaseArgs
         */
        fun of(screen: Pair<FragClazz, BaseArgs>): Destination = Destination().apply { fragments[screen.first] = screen.second }

        /**
         * Create a destination containing multiple [Fragment] screens with an optional typed argument for that screen.
         *
         * @see BaseArgs
         */
        fun of(screens: List<FragClazz>): Destination = Destination().apply { screens.forEach { frag -> fragments[frag] = null } }
    }
}