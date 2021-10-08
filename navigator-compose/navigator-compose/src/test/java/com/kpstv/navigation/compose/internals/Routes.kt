package com.kpstv.navigation.compose.internals

import com.kpstv.navigation.compose.DialogRoute
import com.kpstv.navigation.compose.Route
import kotlinx.parcelize.Parcelize
import kotlin.reflect.KClass

public sealed class TestRoute : Route {
    @Parcelize
    public data class First(val data: String) : TestRoute()
    @Parcelize
    public data class Second(private val noArg: String = "") : TestRoute()
    @Parcelize
    public data class Second1(private val noArg: String = "") : TestRoute()
    @Parcelize
    public data class Second2(private val noArg: String = "") : TestRoute()
    @Parcelize
    public data class Second3(private val noArg: String = "") : TestRoute()
    @Parcelize
    public data class Second4(private val noArg: String = "") : TestRoute()
    @Parcelize
    public data class Second5(private val noArg: String = "") : TestRoute()
    internal companion object {
        val key = TestRoute::class
    }
}

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