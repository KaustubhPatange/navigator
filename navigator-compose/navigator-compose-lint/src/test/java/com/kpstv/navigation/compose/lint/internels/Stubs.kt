package com.kpstv.navigation.compose.lint.internels

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin

object Stubs {
    val RouteClass: TestFile = kotlin(
        """
        package com.kpstv.navigation.compose
        interface Route
        """
    )
}