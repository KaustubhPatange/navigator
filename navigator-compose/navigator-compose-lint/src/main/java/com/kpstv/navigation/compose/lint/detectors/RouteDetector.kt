package com.kpstv.navigation.compose.lint.detectors

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.*
import com.intellij.psi.PsiClass
import org.jetbrains.uast.UClass

class RouteDetector : Detector(), Detector.UastScanner {
    override fun getApplicableUastTypes() = listOf(UClass::class.java)

    private val routes: ArrayList<String> = arrayListOf()

    override fun createUastHandler(context: JavaContext): UElementHandler? {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                if (node.uastSuperTypes.any { it.getQualifiedName() == COMPOSE_ROUTE_CLASS }) {
                    if (context.phase == 1) {
                        routes.add(node.qualifiedName!!)
                    }
                } else if (node.uastSuperTypes.any { routes.contains(it.getQualifiedName()) }) {
                    node.scanForProhibitClasses(context)
                }
            }
        }
    }

    private fun UClass.scanForProhibitClasses(context: JavaContext) {
        if (constructors.flatMap { it.parameters.toList() }.isEmpty()){
            context.report(
                issue = PROHIBIT_OBJECT_CLASS_ISSUE,
                location = context.getRangeLocation(this.navigationElement, 0, textLength),
                message = PROHIBIT_OBJECT_CLASS_ISSUE.getBriefDescription(TextFormat.TEXT),
                quickfixData = LintFix.create()
                    .replace()
                    .name("Convert to an empty-arg destination")
                    .pattern("object $name")
                    .with("data class $name(private val noArg: String = \"\")")
                    .reformat(true)
                    .build()
            )
        }
    }

    companion object {
        private const val COMPOSE_ROUTE_CLASS = "com.kpstv.navigation.compose.Route"
        val PROHIBIT_OBJECT_CLASS_ISSUE = Issue.create(
            id = "prohibitObjectClass",
            briefDescription = "The Route must not contain `object` or `class` as SaveableStateProvider functionality does not work when the app is restored from process death.",
            explanation = """
                The "Route" sealed class must not use an `object` or `class` to represent a no-arg destination.
                If you want to do so then use a `data` class with a single `private` variable that has a default value.
                The problem lies due to incorrect restoration of `rememberSaveable`s value after process death.
                See issue #5 on Github for more detail.
            """.trimIndent(),
            moreInfo = "https://github.com/KaustubhPatange/navigator/issues/5",
            category = Category.CORRECTNESS,
            severity = Severity.ERROR,
            priority = 10,
            androidSpecific = true,
            implementation = Implementation(
                RouteDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}