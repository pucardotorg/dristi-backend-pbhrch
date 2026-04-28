/**
 * Shared model types (AuditDetails, ResponseInfo) used across DRISTI domain modules.
 * Marked as a named interface so other modules can depend on these types
 * without violating Spring Modulith's "no internal cross-module access" rule.
 */
@org.springframework.modulith.NamedInterface("models")
package org.pucar.dristi.common.models;
