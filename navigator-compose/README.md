# navigator-compose

[![CI](https://github.com/KaustubhPatange/navigator/actions/workflows/build-compose.yml/badge.svg)](https://github.com/KaustubhPatange/navigator/actions/workflows/build-compose.yml)
![Maven Central](https://img.shields.io/maven-central/v/io.github.kaustubhpatange/navigator-compose)

An implementation of [`navigator`](https://github.com/KaustubhPatange/navigator) for Jetpack Compose to handle navigation.

**What are the benefits?**

- Handles forward, backward navigation supporting multiple backstack.
- Automatically handles backpress events (to go up the stack).
- No Fragments needed, pure `@Composable` navigation.
- The library also provides some built-in animations when navigating to other screens (check the sample app).
- All the navigation states are preserved across configuration change & process death.

## Implementation

```groovy
// root's build.gradle
allprojects {
    repositories {
        mavenCentral()
    }
}
```

```groovy
// module's build.gradle
dependencies {
    implementation "io.github.kaustubhpatange:navigator-compose:<version>"
}
```

## Samples

- [Basic sample](/samples/basic-sample) - Demonstrates forward & backward navigation with animations as well as setup of Bottom Navigation supporting multiple backstack.
- [JetNews](https://github.com/KaustubhPatange/compose-samples) - A fork from official samples which uses `navigator-compose` for Navigation.

## Tutorials

- [Quick Setup & usage](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Setup)
- [Hands on Tutorials](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials)
  - [Navigating with arguments](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials#navigating-with-arguments)
  - [Navigating with animation](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials#navigating-with-animation)
  - [Navigate with single top instance & `popUpTo`](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials#navigate-with-single-top-instance-&-popUpTo)
  - [Implementing Nested Navigation](https://github.com/KaustubhPatange/navigator/wiki/Compose-Navigator-Tutorials#implementing-nested-navigation)

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
