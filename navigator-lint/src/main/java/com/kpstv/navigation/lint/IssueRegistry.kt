package com.kpstv.navigation.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue
import com.kpstv.navigation.lint.detectors.NavigatorDetector

class IssueRegistry : IssueRegistry() {

    override val api: Int = CURRENT_API

    override val minApi: Int
        get() = 8

    override val issues: List<Issue>
        get() = listOf(
            NavigatorDetector.NAVTRANSMITTER_ISSUE,
            NavigatorDetector.BACKPRESS_NOT_SET_ISSUE
        )
}