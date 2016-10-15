/*
 * Copyright (c) 2015 GitHub
 * Portions copyright (c) 2015 Crate.IO.
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

import org.elasticsearch.Version;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.component.AbstractComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.discovery.zen.ping.unicast.UnicastHostsProvider;
import org.elasticsearch.transport.TransportService;
import org.xbill.DNS.*;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class SrvUnicastHostsProvider extends AbstractComponent implements UnicastHostsProvider {

    public static final String DISCOVERY_SRV_QUERY = "discovery.srv.query";
    public static final String DISCOVERY_SRV_SERVERS = "discovery.srv.servers";

    public static final String DISCOVERY_SRV_PROTOCOL = "discovery.srv.protocol";

    private final TransportService transportService;

    private final Version version;

    private final String query;
    private final Resolver resolver;

    @Inject
    public SrvUnicastHostsProvider(Settings settings, TransportService transportService, Version version) {
        super(settings);
        this.transportService = transportService;
        this.version = version;

        this.query = settings.get(DISCOVERY_SRV_QUERY);
        logger.debug("Using query {}", this.query);
        this.resolver = buildResolver(settings);
    }

    @Nullable
    protected Resolver buildResolver(Settings settings) {
        String[] addresses = settings.getAsArray(DISCOVERY_SRV_SERVERS);
        logger.debug("Using servers {}", addresses);

        // Use tcp by default since it retrieves all records
        String protocol = settings.get(DISCOVERY_SRV_PROTOCOL, "tcp");
        logger.debug("Using protocol {}", protocol);

        List<Resolver> resolvers = new ArrayList();

        for (String address : addresses) {
            String host = null;
            int port = -1;
            String[] parts = address.split(":");
            if (parts.length > 0) {
                host = parts[0];
                if (parts.length > 1) {
                    try {
                        port = Integer.valueOf(parts[1]);
                    } catch (Exception e) {
                        logger.warn("Resolver port '{}' is not an integer. Using default port 53", parts[1]);
                    }
                }

            }

            try {
                Resolver resolver = new SimpleResolver(host);
                if (port > 0) {
                    resolver.setPort(port);
                }
                resolvers.add(resolver);
                logger.debug("Added a resolver for host {} port {}", host, port);
            } catch (UnknownHostException e) {
                logger.warn("Could not create resolver for '{}'", address, e);
            }
        }

        Resolver parent_resolver = null;

        if (resolvers.size() > 0) {
            try {
                parent_resolver = new ExtendedResolver(resolvers.toArray(new Resolver[resolvers.size()]));

                if (protocol.equals("tcp")) {
                    parent_resolver.setTCP(true);
                }

                logger.debug("Created an ExtendedResolver using {} resolvers and protocol {}", resolvers.size(), protocol);
            } catch (UnknownHostException e) {
                logger.warn("Could not create resolver. Using default resolver.", e);
            }
        }

        parent_resolver = parent_resolver == null ? Lookup.getDefaultResolver() : parent_resolver;

        if (protocol == "tcp") {
            parent_resolver.setTCP(true);
        }

        return parent_resolver;
    }

    public List<DiscoveryNode> buildDynamicNodes() {
        List<DiscoveryNode> discoNodes = new ArrayList();
        if (query == null) {
            logger.error("DNS query must not be null. Please set '{}'", DISCOVERY_SRV_QUERY);
            return discoNodes;
        }
        try {
            logger.trace("Building dynamic discovery nodes...");
            discoNodes = lookupNodes();
            if (discoNodes.size() == 0) {
                logger.debug("No nodes found");
            }
        } catch (TextParseException e) {
            logger.error("Unable to parse DNS query '{}'", query);
            logger.error("DNS lookup exception:", e);
        }
        logger.debug("Using dynamic discovery nodes {}", discoNodes);
        return discoNodes;
    }

    protected List<Record> lookupRecords(String query, int type) throws TextParseException {
        Lookup lookup = new Lookup(query, type);
        if (this.resolver != null) {
            lookup.setResolver(this.resolver);
        }

        Record[] records = lookup.run();
        return records == null ? new ArrayList<Record>() : Arrays.asList(records);
    }

    protected List<DiscoveryNode> lookupNodes() throws TextParseException {
        List<DiscoveryNode> discoNodes = new ArrayList<DiscoveryNode>();

        for (Record srvRecord : lookupRecords(query, Type.SRV)) {
            logger.trace("Found SRV record {}", srvRecord);
            for (Record aRecord : lookupRecords(((SRVRecord) srvRecord).getTarget().toString(), Type.A)) {
                logger.trace("Found A record {} for SRV record", aRecord, srvRecord);
                String address = ((ARecord) aRecord).getAddress().getHostAddress() + ":" + ((SRVRecord) srvRecord).getPort();
                try {
                    for (TransportAddress transportAddress : transportService.addressesFromString(address, 1)) {
                        logger.trace("adding {}, transport_address {}", address, transportAddress);
                        discoNodes.add(new DiscoveryNode("#srv-" + address + "-" + transportAddress, transportAddress, version.minimumCompatibilityVersion()));
                    }
                } catch (Exception e) {
                    logger.warn("failed to add {}, address {}", e, address);
                }
            }
        }

        return discoNodes;
    }
}
