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

package org.elasticsearch.discovery.srvtest;

import org.elasticsearch.Version;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.discovery.srv.SrvUnicastHostsProvider;
import org.elasticsearch.transport.TransportService;
import org.xbill.DNS.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class SrvtestUnicastHostsProvider extends SrvUnicastHostsProvider {
    @Inject
    public SrvtestUnicastHostsProvider(Settings settings, TransportService transportService, Version version) {
        super(settings, transportService, version);
    }

    @Override
    protected Resolver buildResolver(Settings settings) {
        try {
            return new SimpleResolver() {
                @Override
                public Message send(Message query) throws IOException {
                    final String HOSTNAME = "localhost.";

                    if (query.getQuestion().getName().toString().equals(Constants.TEST_QUERY)) {
                        Record question = Record.newRecord(query.getQuestion().getName(), Type.SRV, DClass.IN);
                        Message queryMessage = Message.newQuery(question);

                        Message result = new Message();
                        result.setHeader(queryMessage.getHeader());
                        result.addRecord(question, Section.QUESTION);

                        result.addRecord(new SRVRecord(query.getQuestion().getName(), DClass.IN, 1, 1, 1, Constants.NODE_0_TRANSPORT_TCP_PORT, Name.fromString(HOSTNAME)), Section.ANSWER);
                        result.addRecord(new SRVRecord(query.getQuestion().getName(), DClass.IN, 1, 1, 1, Constants.NODE_1_TRANSPORT_TCP_PORT, Name.fromString(HOSTNAME)), Section.ANSWER);
                        result.addRecord(new SRVRecord(query.getQuestion().getName(), DClass.IN, 1, 1, 1, Constants.NODE_2_TRANSPORT_TCP_PORT, Name.fromString(HOSTNAME)), Section.ANSWER);
                        result.addRecord(new SRVRecord(query.getQuestion().getName(), DClass.IN, 1, 1, 1, Constants.NODE_3_TRANSPORT_TCP_PORT, Name.fromString(HOSTNAME)), Section.ANSWER);
                        result.addRecord(new SRVRecord(query.getQuestion().getName(), DClass.IN, 1, 1, 1, Constants.NODE_4_TRANSPORT_TCP_PORT, Name.fromString(HOSTNAME)), Section.ANSWER);
                        return result;
                    }

                    if (query.getQuestion().getName().toString().equals(HOSTNAME)) {
                        Record question = Record.newRecord(query.getQuestion().getName(), Type.A, DClass.IN);
                        Message queryMessage = Message.newQuery(question);

                        Message result = new Message();
                        result.setHeader(queryMessage.getHeader());
                        result.addRecord(question, Section.QUESTION);

                        result.addRecord(new ARecord(query.getQuestion().getName(), DClass.IN, 1, InetAddress.getLoopbackAddress()), Section.ANSWER);
                        return result;
                    }

                    throw new IllegalArgumentException("Unknown test query: " + query.getQuestion().getName().toString());
                }
            };
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
