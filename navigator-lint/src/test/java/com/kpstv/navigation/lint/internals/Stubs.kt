package com.kpstv.navigation.lint.internals

import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin

object Stubs {
    val ActivityClass: TestFile = kotlin(
        """
        package androidx.appcompat.app
        open class AppCompatActivity { 
            open fun onBackPressed() {}
        }
        """
    )
    val FragmentClass: TestFile = kotlin(
        """
        package androidx.fragment.app
        open class Fragment {}
        """
    )
    val ValueFragment: TestFile = kotlin(
        """
        package com.kpstv.navigation
        open class ValueFragment : Fragment() {}
        """
    )

    val NavigatorClass: TestFile = kotlin(
        """
        package com.kpstv.navigation
        class Navigator {}
        """
    )
    val NavTransmitterClass: TestFile = kotlin(
        """
        package com.kpstv.navigation
        interface NavigatorTransmitter { }
        """
    )
}