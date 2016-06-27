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

package org.elasticsearch.plugin.discovery.srv;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.plugins.Plugin;
import java.util.Collection;
import java.util.Collections;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.discovery.srv.SrvDiscovery;
import org.elasticsearch.discovery.srv.SrvDiscoveryModule;
import org.elasticsearch.discovery.srv.SrvUnicastHostsProvider;
import org.elasticsearch.discovery.DiscoveryModule;
import org.elasticsearch.common.inject.Module;

public class SrvDiscoveryPlugin extends Plugin {

    private final Settings settings;
    protected final ESLogger logger = Loggers.getLogger(SrvDiscoveryPlugin.class);

    public SrvDiscoveryPlugin(Settings settings) {
        this.settings = settings;
        logger.trace("starting srv discovery plugin...");
    }

    @Override
    public String name() {
        return "srv-discovery";
    }

    @Override
    public String description() {
        return "SRV Discovery Plugin";
    }

    @Override
    public Collection<Module> nodeModules() {
        return Collections.singletonList((Module) new SrvDiscoveryModule(settings));
    }

    public void onModule(DiscoveryModule discoveryModule) {
        discoveryModule.addDiscoveryType("srv", SrvDiscovery.class);
        discoveryModule.addUnicastHostProvider(SrvUnicastHostsProvider.class);
    }

}
