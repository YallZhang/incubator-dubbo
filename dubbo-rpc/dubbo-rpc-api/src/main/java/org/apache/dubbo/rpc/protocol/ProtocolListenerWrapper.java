/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.rpc.protocol;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.ExporterListener;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.InvokerListener;
import org.apache.dubbo.rpc.Protocol;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.listener.ListenerExporterWrapper;
import org.apache.dubbo.rpc.listener.ListenerInvokerWrapper;

import java.util.Collections;
import java.util.List;

/**
 * ListenerProtocol
 */
public class ProtocolListenerWrapper implements Protocol {
    private final static Logger logger = LoggerFactory.getLogger(ProtocolListenerWrapper.class);

    private final Protocol protocol;

    public ProtocolListenerWrapper(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    @Override
    public int getDefaultPort() {
        return protocol.getDefaultPort();
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
            logger.info("执行ProtocolListenerWrapper的export方法");
            Exporter<T> exporter = protocol.export(invoker);
            logger.info("执行ProtocolListenerWrapper的export方法 结束");
            return exporter;
        }
        logger.info("又执行ProtocolListenerWrapper的export方法，并通过SPI机制加载Listener");
        List<ExporterListener> exporterListeners = ExtensionLoader.getExtensionLoader(ExporterListener.class)
                .getActivateExtension(invoker.getUrl(), Constants.EXPORTER_LISTENER_KEY);
        Exporter<T> exporter = protocol.export(invoker);
        Exporter<T> exporterRet = new ListenerExporterWrapper<T>(exporter, Collections.unmodifiableList(exporterListeners));
        logger.info("又执行ProtocolListenerWrapper的export方法，并newListener 结束");
        return exporterRet;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        //logger.info("refer()  url.getProtocol: " + url.getProtocol());

        if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
            //logger.info("执行ProtocolListenerWrapper的refer方法");
            Invoker<T> invoker = protocol.refer(type, url);
            //logger.info("执行ProtocolListenerWrapper的refer方法 结束");
            return invoker;
        }
        //logger.info("url.getProtocol(): " + url.getProtocol() + "不等于registry时， 又执行ProtocolListenerWrapper的refer方法");

        Invoker<T> protocolInvoker = protocol.refer(type, url);
        Invoker<T> invoker = new ListenerInvokerWrapper<>(protocolInvoker,
                Collections.unmodifiableList(
                        ExtensionLoader
                                .getExtensionLoader(InvokerListener.class)
                                .getActivateExtension(url, Constants.INVOKER_LISTENER_KEY)));
        //logger.info("结束 url.protocol不等于registry时， 又执行ProtocolListenerWrapper的refer方法结束");

        return invoker;
    }

    @Override
    public void destroy() {
        protocol.destroy();
    }

}
