package com.kpstv.navigation.lint.detectors

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClass
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
                if (node.extendsListTypes.any { it.isValueFragment() || it.isActivity() }
                    && node.interfaceTypes.all { (it as PsiClassReferenceType).canonicalText != NAVTRANSMITTER_CLASS }) {
                    node.accept(VisitorPattern(context))
                }
            }
        }
    }

    class VisitorPattern(private val context: JavaContext) : AbstractUastVisitor() {
        override fun visitField(node: UField): Boolean {
            if (node.type.canonicalText == NAVIGATOR_CLASS) {
                context.report(
                    issue = NAVTRANSMITTER_ISSUE,
                    location = context.getNameLocation(node.parent),
                    message = NAVTRANSMITTER_ISSUE.getBriefDescription(TextFormat.TEXT),
                    scope = node.parent,
                    quickfixData = LintFix.create().replace()
                        .name("Add \"NavigatorTransmitter\" interface")
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

    companion object {
        private const val NAVTRANSMITTER_CLASS = "com.kpstv.navigation.NavigatorTransmitter"
        private const val NAVIGATOR_CLASS = "com.kpstv.navigation.Navigator"
        private const val FRAGMENT_CLASS = "androidx.fragment.app.Fragment"

        val NAVTRANSMITTER_ISSUE = Issue.create(
            id = "noNavTransmitter",
            briefDescription = "The host must implement `NavigatorTransmitter` interface.",
            explanation = """
                It seems like you have setup navigator but forgot to apply `NavigatorTransmitter`
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
    }
}