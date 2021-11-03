# Multi-Module navigation sample

The sample outline following things,

- Multi-module navigation using `navigator` with Hilt DI.
- Injecting WorkManager & ViewModel using Hilt.
- Scoped navigation (root only knows `HomeFragment` & `WelcomeFragment` but `home` module manages `HomeStartFragment`, `HomeInternalFragment`, `HomeInternal2Fragment`)

The navigation is implemented in a way that you can easily swap out your navigation library with other library or can directly use `FragmentManager` for transactions.