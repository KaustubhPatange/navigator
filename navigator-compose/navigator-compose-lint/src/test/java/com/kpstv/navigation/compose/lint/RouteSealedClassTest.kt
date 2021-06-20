package com.kpstv.navigation.compose.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.kpstv.navigation.compose.lint.detectors.RouteDetector
import com.kpstv.navigation.compose.lint.internels.Stubs
import org.junit.Test

class RouteSealedClassTest {
    @Test
    fun ProhibitObjectAndClassRoutesInSealedClass() {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.compose.lint
            import com.kpstv.navigation.compose.Route

            sealed class FirstRoute : Route {
                data class First(val data: String) : FirstRoute()
                object None : FirstRoute()
            }
            """
        ).indented()

        TestLintTask.lint().files(
            Stubs.RouteClass,
            stubFile
        )
            .issues(RouteDetector.PROHIBIT_OBJECT_CLASS_ISSUE)
            .run()
            .expect("""
                src/com/kpstv/navigation/compose/lint/FirstRoute.kt:6: Error: The Route must not contain object or class as SaveableStateProvider functionality does not work when the app is restored from process death [prohibitObjectClass]
                    object None : FirstRoute()
                    ~~~~~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
            """.trimIndent())
            .expectFixDiffs("""
                Fix for src/com/kpstv/navigation/compose/lint/FirstRoute.kt line 6: Convert to an empty-arg destination:
                @@ -6 +6
                -     object None : FirstRoute()
                +     data class None(private val noArg: String = "") : FirstRoute()
            """.trimIndent())
    }
}