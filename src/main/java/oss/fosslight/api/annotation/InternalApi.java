/*
 * Copyright (c) 2021 LG Electronics Inc.
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package oss.fosslight.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an API endpoint (or whole controller) as internal-only.
 *
 * <p>Endpoints annotated with {@code @InternalApi} are:
 * <ul>
 *   <li><b>Excluded</b> from the public {@code v2} Swagger group (hidden from external users).</li>
 *   <li><b>Included</b> in the {@code v2-internal} Swagger group, which is only visible to
 *       authenticated {@code ROLE_ADMIN} users (see the custom {@code SwaggerResourcesProvider}
 *       and {@code SwaggerInternalGroupFilter} in {@code SwaggerConfig}).</li>
 *   <li><b>Still callable</b> at runtime &mdash; the annotation only affects Swagger documentation
 *       visibility, not request handling. Actual access control is enforced inside each controller
 *       (e.g. {@code userService.isAdmin(authorization)}).</li>
 * </ul>
 *
 * <p>This annotation can be placed at the class level (applies to all endpoints in the controller)
 * or at the method level (applies to a single endpoint).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface InternalApi {
}
