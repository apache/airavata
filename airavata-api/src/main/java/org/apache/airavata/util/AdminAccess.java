/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.util;

import org.apache.airavata.config.RequestContext;
import org.apache.airavata.exception.ServiceAuthorizationException;

/**
 * Coarse gateway-admin authorization checks for service beans, keyed on the caller's verified realm roles
 * ({@code admin-rw} / {@code admin-ro}). This guards gateway-wide admin operations; per-entity access stays in
 * the sharing registry ({@link SharingHelper#userHasAccess}).
 */
public final class AdminAccess {

    private AdminAccess() {}

    /** Requires the read-write gateway-admin role; throws PERMISSION_DENIED otherwise. */
    public static void requireGatewayAdmin(RequestContext ctx) throws ServiceAuthorizationException {
        if (ctx == null || !ctx.isGatewayAdmin()) {
            throw new ServiceAuthorizationException("Operation requires the gateway admin (admin-rw) role");
        }
    }

    /** Requires the read-write or read-only gateway-admin role; throws PERMISSION_DENIED otherwise. */
    public static void requireAdminOrReadOnly(RequestContext ctx) throws ServiceAuthorizationException {
        if (ctx == null || !(ctx.isGatewayAdmin() || ctx.isReadOnlyGatewayAdmin())) {
            throw new ServiceAuthorizationException("Operation requires a gateway admin (admin-rw or admin-ro) role");
        }
    }
}
