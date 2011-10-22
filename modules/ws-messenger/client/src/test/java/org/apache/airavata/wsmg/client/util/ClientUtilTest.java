package org.apache.airavata.wsmg.client.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClientUtilTest {

    /**
     * Test method for {@link org.apache.airavata.wsmg.util.WsmgUtil#formatURLString(java.lang.String)}.
     */
    @Test
    public void testFormatURLString() {

        String url = "http://www.test.com/unit_test";

        assertSame(url, ClientUtil.formatURLString(url));

        url = "scp://test/test";

        assertSame(url, ClientUtil.formatURLString(url));

        url = "test/test";

        assertTrue(ClientUtil.formatURLString(url).startsWith("http://"));

    }

    /**
     * Test method for {@link org.apache.airavata.wsmg.util.WsmgUtil#getHostIP()}.
     */
    @Test
    public void testGetHostIP() {
        assertNotNull(ClientUtil.getHostIP());
    }

}
