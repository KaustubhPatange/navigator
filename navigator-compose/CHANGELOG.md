# Changelog

## Unreleased

- **Updated**: Jetpack Compose to version `v1.0.3` (requires Kotlin `v1.5.30`).
- **Breaking Change**: `popUpTo` doesn't require destination instance, instead they now accept `Route` keys.
- **Added**: `goBackUntil(dest)` similar to popUntil for `Controller<T>` as well as navigator.
- **Added**: Exposed `navigator.goBack()` for managing backstack or manual handling of `onBackPressed()`.

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
