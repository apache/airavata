package org.apache.airavata.service.context;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RequestContextTest {

    @Test
    void constructorSetsFields() {
        RequestContext ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("role", "admin"));
        assertEquals("testUser", ctx.getUserId());
        assertEquals("testGateway", ctx.getGatewayId());
        assertEquals("token123", ctx.getAccessToken());
        assertEquals("admin", ctx.getClaims().get("role"));
    }

    @Test
    void claimsMapIsUnmodifiable() {
        RequestContext ctx = new RequestContext("u", "g", "t", Map.of("k", "v"));
        assertThrows(UnsupportedOperationException.class, () -> ctx.getClaims().put("new", "val"));
    }
}
