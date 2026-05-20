// HAND-CURATED — Rule 31 boundary marker. Analytics is a read-side
// indexer; peer subdomains (hearing/task/taskmanagement) currently call
// /analytics/pending_task/v1/create via REST (writes — Rule 35 deferred
// to follow-up PR after this analytics PR merges, which would expose a
// typed write API on AnalyticsApi).
@org.springframework.modulith.ApplicationModule(displayName = "Analytics")
package org.pucar.dristi.caselifecycle.analytics;
