package com.kpstv.navigation.compose.internels

import com.kpstv.navigation.compose.DialogRoute
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