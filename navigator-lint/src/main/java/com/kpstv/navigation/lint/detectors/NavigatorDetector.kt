package com.kpstv.navigation.lint.detectors

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.impl.source.PsiClassReferenceType
import com.kpstv.navigation.lint.utils.isActivity
import com.kpstv.navigation.lint.utils.isValueFragment
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UField
import org.jetbrains.uast.visitor.AbstractUastVisitor

class NavigatorDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes() = listOf(UClass::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                if (node.extendsListTypes.any { it.isValueFragment() || it.isActivity() } && !node.hasNavTransmitterInterface()) {
                    node.accept(NavTransmitterVisitorPattern(context))
                }
                if (node.extendsListTypes.any { it.isActivity() } && node.hasNavTransmitterInterface()) {
                    node.checkForBackPressMethodImpl(context)
                }
            }
        }
    }

    private fun UClass.checkForBackPressMethodImpl(context: JavaContext) {
        if (methods.all { it.name != "onBackPressed" }) {
            context.report(
                issue = BACKPRESS_NOT_SET_ISSUE,
                location = context.getNameLocation(this),
                message = BACKPRESS_NOT_SET_ISSUE.getBriefDescription(TextFormat.TEXT)
            )
        }
    }

    class NavTransmitterVisitorPattern(private val context: JavaContext) : AbstractUastVisitor() {
        override fun visitField(node: UField): Boolean {
            if (node.type.canonicalText == FRAGMENT_NAVIGATOR_CLASS) {
                context.report(
                    issue = NAVTRANSMITTER_ISSUE,
                    location = context.getNameLocation(node.parent),
                    message = NAVTRANSMITTER_ISSUE.getBriefDescription(TextFormat.TEXT),
                    scope = node.parent,
                    quickfixData = LintFix.create().replace()
                        .name("Add \"FragmentNavigator.Transmitter\" interface")
                        .range(context.getLocation(node.parent.navigationElement))
                        .text("{")
                        .with(", $NAVTRANSMITTER_CLASS {")
                        .reformat(true)
                        .shortenNames()
                        .build()
                )
            }
            return super.visitField(node)
        }
    }

    private fun UClass.hasNavTransmitterInterface() : Boolean {
        return interfaceTypes.any { (it as PsiClassReferenceType).canonicalText == NAVTRANSMITTER_CLASS }
    }

    companion object {
        private const val NAVTRANSMITTER_CLASS = "com.kpstv.navigation.FragmentNavigator.Transmitter"
        private const val FRAGMENT_NAVIGATOR_CLASS = "com.kpstv.navigation.FragmentNavigator"
        private const val FRAGMENT_CLASS = "androidx.fragment.app.Fragment"

        val NAVTRANSMITTER_ISSUE = Issue.create(
            id = "noNavTransmitter",
            briefDescription = "The host must implement `FragmentNavigator.Transmitter` interface.",
            explanation = """
                It seems like you have setup navigator but forgot to apply "FragmentNavigator.Transmitter"
                interface to this host class. This is very much needed to propagate parent
                navigator instance to the child fragments which ensures the correct behavior
                or backpress, etc.
                """.trimIndent(),
            moreInfo = "https://github.com/KaustubhPatange/navigator/wiki/(Sample-1)-Quick-setup-&-usage",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            priority = 10,
            androidSpecific = true,
            implementation = Implementation(
                NavigatorDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val BACKPRESS_NOT_SET_ISSUE = Issue.create(
            id = "overrideBackPress",
            briefDescription = "The activity must override onBackPressed callback to manually call appropriate Navigator's methods for proper back navigation.",
            explanation = """
                It seems like you have setup navigator in the activity but haven't override onBackPressed.
                You need to override that method & properly ensure if there Navigator can go back. This is
                necessary to ensure the correct behavior of calling onBackPress on child fragments as well
                as making sure multiple backStacks work correct.
            """.trimIndent(),
            moreInfo = "https://github.com/KaustubhPatange/navigator/wiki/(Sample-1)-Quick-setup-&-usage",
            category = Category.CORRECTNESS,
            severity = Severity.WARNING,
            priority = 10,
            androidSpecific = true,
            implementation = Implementation(
                NavigatorDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}