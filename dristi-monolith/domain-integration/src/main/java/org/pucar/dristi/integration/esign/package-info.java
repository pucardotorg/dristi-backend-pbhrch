/**
 * Esign subdomain — eSign integration (ESP protocol XML signing flow plus
 * digital-signature PDF stamping).
 *
 * <p>Marked as a Spring Modulith application module. Other modules
 * (order-management today) MUST consume esign through
 * {@link org.pucar.dristi.integration.esign.EsignApi}; reaching into
 * {@code internal/} is a structural violation enforced by
 * {@code ModuleStructureTest.verify()}.
 *
 * <p>Contract DTOs live at {@code dristi-common/contract/esign/}.
 */
@org.springframework.modulith.NamedInterface
@org.springframework.modulith.ApplicationModule(displayName = "Esign")
package org.pucar.dristi.integration.esign;
