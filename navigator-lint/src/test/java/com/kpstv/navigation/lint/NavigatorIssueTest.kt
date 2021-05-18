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

            open class AbstractValueFragment : ValueFragment() {}

            class MyFragment : AbstractValueFragment() {
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
                src/com/kpstv/navigation/lint/AbstractValueFragment.kt:8: Warning: The host must implement NavigatorTransmitter interface. [noNavTransmitter]
                class MyFragment : AbstractValueFragment() {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
            .expectFixDiffs("""
                Fix for src/com/kpstv/navigation/lint/AbstractValueFragment.kt line 8: Add "NavigatorTransmitter" interface:
                @@ -8 +8
                - class MyFragment : AbstractValueFragment() {
                + class MyFragment : AbstractValueFragment() , NavigatorTransmitter {
            """.trimIndent())
    }

    @Test
    fun CheckIfNavTransmitterIsPresentForActivity() {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.lint

            import com.kpstv.navigation.Navigator
            import androidx.appcompat.app.AppCompatActivity

            open class AbstractActivity: AppCompatActivity() {}

            class MyActivity : AbstractActivity() {
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
                src/com/kpstv/navigation/lint/AbstractActivity.kt:8: Warning: The host must implement NavigatorTransmitter interface. [noNavTransmitter]
                class MyActivity : AbstractActivity() {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
            .expectFixDiffs("""
                Fix for src/com/kpstv/navigation/lint/AbstractActivity.kt line 8: Add "NavigatorTransmitter" interface:
                @@ -8 +8
                - class MyActivity : AbstractActivity() {
                + class MyActivity : AbstractActivity() , NavigatorTransmitter {
            """.trimIndent())
    }

    @Test
    fun CheckIfActivityHasOverrideBackPress() {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.lint

            import com.kpstv.navigation.Navigator
            import com.kpstv.navigation.NavigatorTransmitter
            import androidx.appcompat.app.AppCompatActivity

            open class AbstractActivity: AppCompatActivity() {}

            class MyActivity : AbstractActivity(), NavigatorTransmitter {
                private lateinit var navigator: Navigator
            }
            """
        ).indented()

        TestLintTask.lint().files(
            Stubs.ActivityClass,
            Stubs.NavTransmitterClass,
            Stubs.NavigatorClass,
            stubFile,
        )
            .issues(NavigatorDetector.BACKPRESS_NOT_SET_ISSUE)
            .run()
            .expect("""
                src/com/kpstv/navigation/lint/AbstractActivity.kt:9: Warning: The activity must override onBackPressed callback to manually call appropriate Navigator's methods for proper back navigation. [overrideBackPress]
                class MyActivity : AbstractActivity(), NavigatorTransmitter {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
    }
}