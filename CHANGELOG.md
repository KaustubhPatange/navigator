# Changelog

## Version `0.1-alpha41`

- **Change**: In `ViewRetaintion.RETAIN` mode, fragments are set to `RESUMED` state when they are active & `PAUSED` state when they are in active.

## Version `0.1-alpha39`

- **Breaking Change**: `NavOptions.remember` will be `true` by default.
- **Fixed**: Some leaks found in `navigator` & `navigator-base-navigation` libraries.

## Version `0.1-alpha37`

- **Change**: Unified version for all the add-on libraries.

## Version `0.1-alpha36`

- **Fixed**: `StateViewModel`'s history will also be considered during initial destination(s) creation.

## Version `0.1-alpha35`

- **Added**: Methods for passing an instance of fragment for `navigateTo` & `show` call.

## Version `0.1-alpha34` 

- **Fixed**: Issue where `navigateTo` a `DialogFragment` crashes the app.

## Version `0.1-alpha32` _(2021-10-20)_

- **Breaking Change**: `getParentNavigator()` & `getSimpleNavigator()` have become property types.

## Version `0.1-alpha30` _(2021-08-03)_

- **Fixed**: `IllegalStateException` when circularTransform runs before container's root view is laid out or is in layout.
