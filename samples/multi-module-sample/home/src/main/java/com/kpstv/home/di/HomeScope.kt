package com.kpstv.home.di

import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.migration.AliasOf
import javax.inject.Scope

@Scope
@AliasOf(ActivityScoped::class)
annotation class HomeScope()
