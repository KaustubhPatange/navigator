package com.kpstv.navigation.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.kpstv.navigation.lint.detectors.NavigatorDetector
import com.kpstv.navigation.lint.internals.Stubs
import org.junit.Test

class FragmentNavigatorIssueTest {

    @Test
    fun CheckIfNavTransmitterIsPresentForFragment () {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.lint
            import com.kpstv.navigation.FragmentNavigator
            import com.kpstv.navigation.ValueFragment
            import com.kpstv.navigation.NavigatorTransmitter

            open class AbstractValueFragment : ValueFragment() {}

            class MyFragment : AbstractValueFragment() {
                private lateinit var navigator: FragmentNavigator
            }
            """
        ).indented()

        TestLintTask.lint().files(
            Stubs.FragmentClass,
            Stubs.FragmentNavigatorClass,
            Stubs.ValueFragment,
            stubFile,
        )
            .issues(NavigatorDetector.NAVTRANSMITTER_ISSUE)
            .run()
            .expect("""
                src/com/kpstv/navigation/lint/AbstractValueFragment.kt:8: Warning: The host must implement FragmentNavigator.Transmitter interface. [noNavTransmitter]
                class MyFragment : AbstractValueFragment() {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
            .expectFixDiffs("""
                Fix for src/com/kpstv/navigation/lint/AbstractValueFragment.kt line 8: Add "FragmentNavigator.Transmitter" interface:
                @@ -8 +8
                - class MyFragment : AbstractValueFragment() {
                + class MyFragment : AbstractValueFragment() , FragmentNavigator.Transmitter {
            """.trimIndent())
    }

    @Test
    fun CheckIfNavTransmitterIsPresentForActivity() {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.lint

            import com.kpstv.navigation.FragmentNavigator
            import androidx.appcompat.app.AppCompatActivity

            open class AbstractActivity: AppCompatActivity() {}

            class MyActivity : AbstractActivity() {
                private lateinit var navigator: FragmentNavigator
            }
            """
        ).indented()

        TestLintTask.lint().files(
            Stubs.ActivityClass,
            Stubs.FragmentNavigatorClass,
            Stubs.ValueFragment,
            stubFile,
        )
            .issues(NavigatorDetector.NAVTRANSMITTER_ISSUE)
            .run()
            .expect("""
                src/com/kpstv/navigation/lint/AbstractActivity.kt:8: Warning: The host must implement FragmentNavigator.Transmitter interface. [noNavTransmitter]
                class MyActivity : AbstractActivity() {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
            .expectFixDiffs("""
                Fix for src/com/kpstv/navigation/lint/AbstractActivity.kt line 8: Add "FragmentNavigator.Transmitter" interface:
                @@ -8 +8
                - class MyActivity : AbstractActivity() {
                + class MyActivity : AbstractActivity() , FragmentNavigator.Transmitter {
            """.trimIndent())
    }

    @Test
    fun CheckIfActivityHasOverrideBackPress() {
        val stubFile = LintDetectorTest.kotlin(
            """
            package com.kpstv.navigation.lint

            import com.kpstv.navigation.FragmentNavigator
            import com.kpstv.navigation.FragmentNavigator.Transmitter
            import androidx.appcompat.app.AppCompatActivity

            open class AbstractActivity: AppCompatActivity() {}

            class MyActivity : AbstractActivity(), FragmentNavigator.Transmitter {
                private lateinit var navigator: FragmentNavigator
            }
            """
        ).indented()

        TestLintTask.lint().files(
            Stubs.ActivityClass,
            Stubs.FragmentNavigatorClass,
            stubFile,
        )
            .issues(NavigatorDetector.BACKPRESS_NOT_SET_ISSUE)
            .run()
            .expect("""
                src/com/kpstv/navigation/lint/AbstractActivity.kt:9: Warning: The activity must override onBackPressed callback to manually call appropriate Navigator's methods for proper back navigation. [overrideBackPress]
                class MyActivity : AbstractActivity(), FragmentNavigator.Transmitter {
                      ~~~~~~~~~~
                0 errors, 1 warnings
            """.trimIndent())
    }
}