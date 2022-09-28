# Changelog

## Version `0.1-alpha36` _(2022-09-29)_

- **Fixed**: Proper lifecycle handling/creation on destination change.

## Version `0.1-alpha35` _(2022-08-14)_

- **Fixed**: Lifecycle state changes with activity state changes.

## Version `0.1-alpha33` _(2022-07-03)_

- **Improved**: Checking multiple dialog instance will now account for `equals()`.

## Version `0.1-alpha32` _(2022-06-06)_

- **Added**: `currentRoute` & `parentRoute` properties on `Controller<T>`.

## Version `0.1-alpha31` _(2022-05-10)_

- **Fixed**: Proper lifecycle events on `Route`.

## Version `0.1-alpha30` _(2022-05-02)_

- **Updated**: Jetpack Compose to version v1.1.1 (requires Kotlin v1.6.10).

## Version `0.1-alpha28` _(2022-05-02)_

- **Fixed**: Scoped `ViewModel`s `savedStateBundle` does not save across multiple screens.

## Version `0.1-alpha27` _(2022-04-01)_

- **Added**: Support for scoped `ViewModel`s (#20).
- **Updated**: Jetpack Compose to version v1.0.4 (requires Kotlin v1.5.31).
- **Breaking Change**: The signature of `ComposeNavigator.Setup(...)` is changed as some parameters are reordered.

## Version `0.1-alpha26` _(2022-03-20)_

- **Breaking Change**: Removed `suppressBackPress` as there is no real use-case for it.
- **Fixed**: `goBackUntil` doesn't correct remove keys from `saveableStateHolder` upon pop.

## Version `0.1-alpha25` _(2021-10-23)_

- **Breaking Change**: Renamed `findController` to `findNavController`.
- **Breaking Change**: Navigation Route is not tied to the class. Instead defined key using `Route.Key`.

## Version `0.1-alpha24` _(2021-10-08)_

- **Breaking Change**: `popUpTo` doesn't require destination instance, instead they now accept `Route` keys.
- **Behavioral Change** `popUpTo`'s inclusive parameter is now `false` by default.
- **Added**: `goBackUntil(dest)` similar to popUntil for `Controller<T>` as well as navigator.
- **Added**: `goBackToRoot()` i.e jump to root destination functionality for `Controller<T>` as well as navigator.
- **Added**: Exposed `navigator.goBack()` for managing backstack or manual handling of `onBackPressed()`.

## Version `0.1-alpha23` _(2021-10-05)_

- **Breaking Change**: Implementing a custom transition now requires to explicitly specify `TransitionKey`.
- **Updated**: Jetpack Compose to version v1.0.3 (requires Kotlin v1.5.30).
- **Fixed**: Transitions not working when R8 in full mode is enabled.

## Version `0.1-alpha22` _(2021-09-29)_

- **Breaking Change**: `rememberController` renamed to `rememberNavController`.
- **Breaking Change**: You must provide `Controller<T>` using `rememberNavController<T>()` during `Setup`. This removes the need of `controller` parameter scope in the content.

## Version `0.1-alpha21` _(2021-09-11)_

- **Breaking Change**: Dismiss() in `DialogScope` does not respects the `dialogNavigator`'s backstack, use `goBack()`.
- **Added**: `canGoBack()` to determine if backward navigation is possible or not.
- **Updated**: Jetpack Compose to version `v1.0.2`.
- **Fixed**: CompositionLocalScope will not be properly removed when composition disposes.
- **Fixed**: `getAllHistory()` on empty backstack causes Collection empty exception.

## Version `0.1-alpha20` _(2021-09-05)_

- **Added**: `getCurrentRouteAsFlow` method to observe changes to current destination.

## Version `0.1-alpha19` _(2021-09-01)_

- **Added**: `handleOnDismissRequest` API in `CreateDialog` for intercepting dismiss request.
- **Fixed**: Incorrect popping of dialogs during onBackPressed().

## Version `0.1-alpha18` _(2021-08-31)_

- **Breaking Change**: Renamed `setDefaultBackPressEnabled(boolean)` to `disableDefaultBackPressLogic()`.
- **Breaking Change**: Creating dialogs through controller will now provide `DialogScope` (read [here](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials#implementing-dialogs)).
- **Added:** Support for navigation inside Dialogs (read [here](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials#navigation-in-dialogs)).

## Version `0.1-alpha17` _(2021-08-21)_

- **Updated**: Java Docs for animations.
- **Updated**: Updated Compose to v1.0.1

## Version `0.1-alpha15` _(2021-08-01)_

- **Added**: Support for `Dialog` destination (read [here](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials#implementing-dialogs)).

## Version `0.1-alpha14` _(2021-07-29)_

- **Updated**: Compose version to 1.0.0 stable release.
