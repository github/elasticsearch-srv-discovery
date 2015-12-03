/*
 * Copyright (c) 2015 GitHub
 *
 *     Permission is hereby granted, free of charge, to any person obtaining
 *     a copy of this software and associated documentation files (the "Software"),
 *     to deal in the Software without restriction, including without limitation
 *     the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *     and/or sell copies of the Software, and to permit persons to whom the Software
 *     is furnished to do so, subject to the following conditions:
 *
 *     The above copyright notice and this permission notice shall be included in
 *     all copies or substantial portions of the Software.
 *
 *     THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *     EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *     OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *     IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 *     CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 *     TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE
 *     OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.elasticsearch.discovery.srv;

import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.discovery.srvtest.Constants;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.elasticsearch.test.ElasticsearchIntegrationTest.ClusterScope;
import org.junit.Test;
import org.xbill.DNS.Resolver;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

@ClusterScope(scope = ElasticsearchIntegrationTest.Scope.SUITE, numDataNodes = 0)
public class SrvDiscoveryIntegrationTest extends ElasticsearchIntegrationTest {
    @Test
    public void testClusterSrvDiscoveryWith2Nodes() throws Exception {
        ImmutableSettings.Builder b = settingsBuilder()
            .put("node.mode", "network")
            .put("discovery.zen.ping.multicast.enabled", "false")
            .put("discovery.type", "srvtest")
            .put(SrvUnicastHostsProvider.DISCOVERY_SRV_QUERY, Constants.TEST_QUERY);

        assertEquals(cluster().size(), 0);

        internalCluster().startNode(b.put("transport.tcp.port", String.valueOf(Constants.NODE_0_TRANSPORT_TCP_PORT)).build());
        internalCluster().startNode(b.put("transport.tcp.port", String.valueOf(Constants.NODE_1_TRANSPORT_TCP_PORT)).build());

        assertEquals(cluster().size(), 2);
    }

    @Test
    public void testClusterSrvDiscoveryWith5Nodes() throws Exception {
        ImmutableSettings.Builder b = settingsBuilder()
            .put("node.mode", "network")
            .put("discovery.zen.ping.multicast.enabled", "false")
            .put("discovery.type", "srvtest")
            .put(SrvUnicastHostsProvider.DISCOVERY_SRV_QUERY, Constants.TEST_QUERY);

        assertEquals(cluster().size(), 0);

        internalCluster().startNode(b.put("transport.tcp.port", String.valueOf(Constants.NODE_0_TRANSPORT_TCP_PORT)).build());
        internalCluster().startNode(b.put("transport.tcp.port", String.valueOf(Constants.NODE_1_TRANSPORT_TCP_PORT)).build());
        internalCluster().startNode(b.put("transport.tcp.port", String.valueOf(Constants.NODE_2_TRANSPORT_TCP_PORT)).build());
        internalCluster().startNode(b.put("transport.tcp.port", String.valueOf(Constants.NODE_3_TRANSPORT_TCP_PORT)).build());
        internalCluster().startNode(b.put("transport.tcp.port", String.valueOf(Constants.NODE_4_TRANSPORT_TCP_PORT)).build());

        assertEquals(cluster().size(), 5);
    }

    @Test
    public void testResolverStillDefaultsToTcpWhenNoServersAreGiven() throws Exception {
        ImmutableSettings.Builder b = settingsBuilder()
            .put("node.mode", "network")
            .put("discovery.zen.ping.multicast.enabled", "false")
            .put("discovery.type", "srv")
            .put(SrvUnicastHostsProvider.DISCOVERY_SRV_QUERY, "");

        SrvUnicastHostsProvider provider = new SrvUnicastHostsProvider(b.build(), null, null);

        assertTrue(provider.usingTCP);
    }
}
