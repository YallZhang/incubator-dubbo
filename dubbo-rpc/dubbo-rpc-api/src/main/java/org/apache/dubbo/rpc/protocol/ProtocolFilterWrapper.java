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
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Protocol;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

import java.util.List;

/**
 *
 * ListenerProtocol
 */
public class ProtocolFilterWrapper implements Protocol {
    private final static Logger logger = LoggerFactory.getLogger(ProtocolFilterWrapper.class);

    private final Protocol protocol;

    public ProtocolFilterWrapper(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("protocol == null");
        }
        this.protocol = protocol;
    }

    private static <T> Invoker<T> buildInvokerChain(final Invoker<T> invoker, String key, String group) {
        Invoker<T> last = invoker;
        //logger.info("buildInvokerChain()方法内部 开始通过SPI机制获取Filters");
        List<Filter> filters = ExtensionLoader.getExtensionLoader(Filter.class).getActivateExtension(invoker.getUrl(), key, group);
        if (filters.size() > 0) {
            for (int i = filters.size() - 1; i >= 0; i--) {
                final Filter filter = filters.get(i);
                final Invoker<T> next = last;
                last = new Invoker<T>() {

                    @Override
                    public Class<T> getInterface() {
                        return invoker.getInterface();
                    }

                    @Override
                    public URL getUrl() {
                        return invoker.getUrl();
                    }

                    @Override
                    public boolean isAvailable() {
                        return invoker.isAvailable();
                    }

                    @Override
                    public Result invoke(Invocation invocation) throws RpcException {
                        return filter.invoke(next, invocation);
                    }

                    @Override
                    public void destroy() {
                        invoker.destroy();
                    }

                    @Override
                    public String toString() {
                        return invoker.toString();
                    }
                };
            }
        }
        //logger.info("buildInvokerChain()方法 通过SPI机制获取Filters结束");
        return last;
    }

    @Override
    public int getDefaultPort() {
        return protocol.getDefaultPort();
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        if (Constants.REGISTRY_PROTOCOL.equals(invoker.getUrl().getProtocol())) {
            logger.info("执行ProtocolFilterWrapper的export方法");
            Exporter<T> exporter = protocol.export(invoker);
            logger.info("执行ProtocolFilterWrapper的export方法 结束");
            return exporter;
        }
        logger.info("又执行ProtocolFilterWrapper的export方法，并且执行buildInvokerChain");
        Invoker<T> invokerChain = buildInvokerChain(invoker, Constants.SERVICE_FILTER_KEY, Constants.PROVIDER);
        Exporter<T> exporter = protocol.export(invokerChain);
        logger.info("又执行ProtocolFilterWrapper的export方法，并且执行buildInvokerChain 结束");
        return exporter;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        //logger.info("url.getProtocol: " + url.getProtocol());
        if (Constants.REGISTRY_PROTOCOL.equals(url.getProtocol())) {
            //logger.info("执行ProtocolFilterWrapper的refer方法");
            Invoker<T> invoker = protocol.refer(type, url);
            //logger.info("执行ProtocolFilterWrapper的refer方法 结束");
            return invoker;
        }

        //logger.info("开始： url.getProtocol()不等于registry常量时，又执行refer()方法，并执行buildInvokerChain");
        Invoker<T> protocolInvoker = protocol.refer(type, url);
        Invoker<T> invokerRet = buildInvokerChain(protocolInvoker, Constants.REFERENCE_FILTER_KEY, Constants.CONSUMER);
        //logger.info("结束： 又执行refer()方法，并执行buildInvokerChain结束");
        return invokerRet;
    }

    @Override
    public void destroy() {
        protocol.destroy();
    }

}
