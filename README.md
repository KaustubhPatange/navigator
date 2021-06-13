# navigator

![build](https://github.com/KaustubhPatange/navigator/workflows/build/badge.svg)

> **Looking for Jetpack Compose implementation of `navigator`, see [here](navigator-compose)**.

A small (Kotlin first) navigation library for Android to ease the use of fragment transactions with a `navigator.navigateTo`
call (rest is handled for you) & navigating back is as simple as `navigator.goBack()`.

You also get a separate `onBackPressed()` callback on the child fragments which will be called when `this` is the current visible fragment & is the one which is going to be removed upon back press.

The library provides some custom transitions like `CircularTransform` (on top of existing animations), see its
use in the sample app [here](/samples/backpress-sample).

<!-- The library is build on the existing Fragment APIs so it is easy to introduce this library into existing project. The
developer has complete control over navigation & can choose between `FragmentTransaction` or `Navigator` at
any time. -->

If you have any implementation details to cover let me know.

**What additional benefits I can get when using this library?**

- Proper handling of Fragment Transaction.
- Handling up & back press actions efficiently on [Fragment](/samples/basic-sample).
- Easy to use API for animations & transitions (see [here](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-animation)).
- Generic Typed Arguments when passing data between destinations (see [here](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-typed-arguments)).
- Quick Bottom & Tab navigation setup (see [tutorials](#tutorials)).

## Implementation

- The individual library versions can be found [here](https://github.com/KaustubhPatange/navigator/wiki/Setup).

```groovy
// root's build.gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```

```groovy
// modules's build.gradle
dependencies {
    // Check the above link for the individual library versions.
    implementation "io.github.kaustubhpatange:navigator:<version>" // Core library (Required)
    implementation "io.github.kaustubhpatange:navigator-extensions:<version>" // Optional but recommended
    implementation "io.github.kaustubhpatange:navigator-bottom-navigation:<version>" // For setting up Bottom Navigation.
    implementation "io.github.kaustubhpatange:navigator-tab-navigation:<version>" // For setting up Tab Layout Navigation.
    implementation "io.github.kaustubhpatange:navigator-rail-navigation:<version>" // For setting up Rail Navigation.
}
```

## Samples

- [Basic sample](/samples/basic-sample) - Hands on with the introduction to some library features.
- [Backpress sample](/samples/backpress-sample) - A sample focused on handling back press events effectively.
- [Navigation Sample](/samples/navigation-sample) - A sample which demonstrate the use of Bottom & Tab navigation supporting multiple backstack through `navigator`.
- #### **Running the samples**

```
// Run the following command before building the project.

./gradlew navigator:publishReleasePublicationToMavenLocal
```

## Tutorials

- [Quick Setup & usage](<https://github.com/KaustubhPatange/navigator/wiki/(Sample-1)-Quick-setup-&-usage>)
- [Hands on Tutorials](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials)
  - [Navigate to a Fragment](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigate-to-fragment)
  - [Navigate to a Fragment but remember the transaction](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigate-to-a-Fragment-but-remember-the-transaction)
  - [Navigation with typed arguments](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-typed-arguments)
  - [Navigation with animation](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-animation)
  - [Navigation with single top instance or `popUpTo`](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigation-with-single-top-instance-or-popUpTo)
  - [Navigate to a BottomSheet or Dialog Fragment](https://github.com/KaustubhPatange/navigator/wiki/Quick-Tutorials#navigate-to-a-bottomSheet-or-dialog-fragment)
- [Bottom navigation setup](<https://github.com/KaustubhPatange/navigator/wiki/(Sample-2)-Bottom-navigation-setup>)
- [TabLayout navigation setup](<https://github.com/KaustubhPatange/navigator/wiki/(Sample-3)-Tab-Navigation>)
- [Rail navigation setup](<https://github.com/KaustubhPatange/navigator/wiki/(Sample-4)-Rail-Navigation>)

## License

- [The Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)

```
Copyright 2020 Kaustubh Patange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
