package com.kpstv.navigation.compose.internels

import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.Route
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

public class DialogRoutes {
    @Parcelize
    public object FirstDialog: DialogRoute {
        public val key: KClass<FirstDialog> get() = FirstDialog::class
    }

    @Parcelize
    public object SecondDialog: DialogRoute {
        public val key: KClass<SecondDialog> get() = SecondDialog::class
    }

    @Parcelize
    public object ThirdDialog: DialogRoute {
        public val key: KClass<ThirdDialog> get() = ThirdDialog::class
    }

    @Parcelize
    public object ForthDialog: DialogRoute {
        public val key: KClass<ForthDialog> get() = ForthDialog::class
    }
}

public class Routes {
    public sealed class MainRoute : Route {
        @Parcelize
        public data class First(private val noArg: String = "") : MainRoute()
        @Parcelize
        public data class Second(private val noArg: String = "") : MainRoute()
    }
    public sealed class MainFirstRoute : Route {
        @Parcelize
        public data class First(private val noArg: String = "") : MainFirstRoute()
    }
    public sealed class MainSecondRoute : Route {
        @Parcelize
        public data class First(private val noArg: String = "") : MainSecondRoute()
        @Parcelize
        public data class Second(private val noArg: String = "") : MainSecondRoute()
        @Parcelize
        public data class Third(private val noArg: String = "") : MainSecondRoute()
    }
    public sealed class MainSecondFirstRoute : Route {
        @Parcelize
        public data class First(private val noArg: String = "") : MainSecondFirstRoute()
    }
    public sealed class MainSecondThirdRoute : Route {
        @Parcelize
        public data class First(private val noArg: String = "") : MainSecondThirdRoute()
        @Parcelize
        public data class Second(private val noArg: String = "") : MainSecondThirdRoute()
    }
}