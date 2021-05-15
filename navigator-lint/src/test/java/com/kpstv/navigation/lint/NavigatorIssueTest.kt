package com.kpstv.navigation.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.kpstv.navigation.lint.detectors.NavigatorDetector
import com.kpstv.navigation.lint.internals.Stubs
import org.junit.Test

class NavigatorIssueTest {

    @Test
    fun CheckIfNavTransmitterIsPresentForFragment () {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.lint
            import com.kpstv.navigation.Navigator
            import com.kpstv.navigation.ValueFragment
            import com.kpstv.navigation.NavigatorTransmitter

            class MyFragment : ValueFragment() {
                private lateinit var navigator: Navigator
            }
            """
        ).indented()

        TestLintTask.lint().files(
            Stubs.FragmentClass,
            Stubs.NavTransmitterClass,
            Stubs.NavigatorClass,
            Stubs.ValueFragment,
            stubFile,
        )
            .issues(NavigatorDetector.NAVTRANSMITTER_ISSUE)
            .run()
            .expect("""
                src/com/kpstv/navigation/lint/MyFragment.kt:6: Warning: The host must implement NavigatorTransmitter interface. [noNavTransmitter]
                class MyFragment : ValueFragment() {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
    }

    @Test
    fun CheckIfNavTransmitterIsPresentForActivity() {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.lint
            import com.kpstv.navigation.Navigator
            import com.kpstv.navigation.NavigatorTransmitter
            import androidx.appcompat.app.AppCompatActivity

            class MyActivity : AppCompatActivity() {
                private lateinit var navigator: Navigator
            }
            """
        ).indented()

        TestLintTask.lint().files(
            Stubs.ActivityClass,
            Stubs.NavTransmitterClass,
            Stubs.NavigatorClass,
            Stubs.ValueFragment,
            stubFile,
        )
            .issues(NavigatorDetector.NAVTRANSMITTER_ISSUE)
            .run()
            .expect("""
                src/com/kpstv/navigation/lint/MyActivity.kt:6: Warning: The host must implement NavigatorTransmitter interface. [noNavTransmitter]
                class MyActivity : AppCompatActivity() {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
    }
}