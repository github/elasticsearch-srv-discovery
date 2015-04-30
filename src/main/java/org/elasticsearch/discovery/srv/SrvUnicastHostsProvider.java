/*
 * Copyright 2015 Grant Rodgers.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.elasticsearch.discovery.srv;

import org.elasticsearch.Version;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.discovery.zen.ping.unicast.UnicastHostsProvider;
import org.elasticsearch.transport.TransportService;
import org.xbill.DNS.*;

import java.util.List;

/**
 *
 */
public class SrvUnicastHostsProvider extends AbstractComponent implements UnicastHostsProvider {

    private final TransportService transportService;

    private final Version version;

    private final String query;

    @Inject
    public SrvUnicastHostsProvider(Settings settings, TransportService transportService, Version version) {
        super(settings);
        this.transportService = transportService;
        this.version = version;

        this.query = settings.get("discovery.srv.query");
    }

    @Override
    public List<DiscoveryNode> buildDynamicNodes() {
        List<DiscoveryNode> discoNodes = Lists.newArrayList();

        try {
            Record[] records = new Lookup(query, Type.SRV).run();

            logger.trace("building dynamic unicast discovery nodes...");
            if (records == null) {
                logger.debug("No nodes found");
            } else {
                for (Record record : records) {
                    SRVRecord srv = (SRVRecord) record;

                    String hostname = srv.getTarget().toString().replaceFirst("\\.$", "");
                    int port = srv.getPort();
                    String address = hostname + ":" + port;

                    try {
                        TransportAddress[] addresses = transportService.addressesFromString(address);
                        logger.trace("adding {}, transport_address {}", address, addresses[0]);
                        discoNodes.add(new DiscoveryNode("#srv-" + address, addresses[0], version.minimumCompatibilityVersion()));
                    } catch (Exception e) {
                        logger.warn("failed to add {}, address {}", e, address);
                    }
                }
            }


        } catch (TextParseException e) {
            logger.warn("Unable to parse DNS query '{}'", query);
            logger.debug("DNS lookup exception:", e);
        }


        logger.debug("using dynamic discovery nodes {}", discoNodes);

        return discoNodes;
    }
}
