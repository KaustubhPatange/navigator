package com.kpstv.navigation.compose.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.kpstv.navigation.compose.lint.detectors.RouteDetector

class ComposeNavigatorIssueRegistry : IssueRegistry() {
    override val api: Int = CURRENT_API

    override val minApi: Int
        get() = 10

    override val issues: List<Issue>
        get() = listOf(RouteDetector.PROHIBIT_OBJECT_CLASS_ISSUE)

    override val vendor: Vendor = Vendor(
        vendorName = "Kaustubh Patange",
        feedbackUrl = "https://github.com/KaustubhPatange/navigator/issues",
        contact = "https://github.com/KaustubhPatange/navigator"
    )
}