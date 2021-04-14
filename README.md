# navigator

A small navigation library for Android to ease the use of fragment transactions with a `navigator.navigateTo`
call (rest is handled for you) & navigating back is as simple as `navigator.goBack()`.

The library provides some custom transitions like `CircularTransform` (on top of existing animations), see its
use in the sample app [here](/samples).

The library builds up on the existing Fragment APIs so it is easy to introduce this library into existing project. The
developer has complete control over navigation & can choose between `FragmentTransaction` or `Navigator` at
any time.

## Implementation

```groovy
// root's build.gradle
allprojects {
    repositories {
        mavenLocal()
    }
}
```

- Read the complete setup instructions [here](https://github.com/KaustubhPatange/navigator/wiki/Setup).

## Samples

- [Basic sample](/samples/basic-sample) - Hands on with the introduction to some library features.
- [Backpress sample](/samples/backpress-sample) - A sample focused on handling back press events effectively.
- [Bottom Navigation Sample](/samples/bottom-navigation-sample) - A bottom navigation implementation using `navigator`.

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
