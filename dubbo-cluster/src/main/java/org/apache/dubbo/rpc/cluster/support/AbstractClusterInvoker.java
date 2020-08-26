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
package org.apache.dubbo.rpc.cluster.support;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.Version;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcInvocation;
import org.apache.dubbo.rpc.cluster.Directory;
import org.apache.dubbo.rpc.cluster.LoadBalance;
import org.apache.dubbo.rpc.support.RpcUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AbstractClusterInvoker
 */
public abstract class AbstractClusterInvoker<T> implements Invoker<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractClusterInvoker.class);

    protected final Directory<T> directory;

    protected final boolean availablecheck;

    private AtomicBoolean destroyed = new AtomicBoolean(false);

    private volatile Invoker<T> stickyInvoker = null;

    public AbstractClusterInvoker(Directory<T> directory) {
        this(directory, directory.getUrl());
    }

    public AbstractClusterInvoker(Directory<T> directory, URL url) {
        if (directory == null) {
            throw new IllegalArgumentException("service directory == null");
        }

        this.directory = directory;
        //sticky: invoker.isAvailable() should always be checked before using when availablecheck is true.
        this.availablecheck = url.getParameter(Constants.CLUSTER_AVAILABLE_CHECK_KEY, Constants.DEFAULT_CLUSTER_AVAILABLE_CHECK);
    }

    @Override
    public Class<T> getInterface() {
        return directory.getInterface();
    }

    @Override
    public URL getUrl() {
        return directory.getUrl();
    }

    @Override
    public boolean isAvailable() {
        Invoker<T> invoker = stickyInvoker;
        if (invoker != null) {
            return invoker.isAvailable();
        }
        return directory.isAvailable();
    }

    @Override
    public void destroy() {
        if (destroyed.compareAndSet(false, true)) {
            directory.destroy();
        }
    }

    /**
     * Select a invoker using loadbalance policy.</br>
     * a) Firstly, select an invoker using loadbalance. If this invoker is in previously selected list, or,
     * if this invoker is unavailable, then continue step b (reselect), otherwise return the first selected invoker</br>
     * <p>
     * b) Reselection, the validation rule for reselection: selected > available. This rule guarantees that
     * the selected invoker has the minimum chance to be one in the previously selected list, and also
     * guarantees this invoker is available.
     *
     * @param loadbalance load balance policy
     * @param invocation  invocation
     * @param invokers    invoker candidates
     * @param selected    exclude selected invokers or not
     * @return the invoker which will final to do invoke.
     * @throws RpcException exception
     */
    protected Invoker<T> select(LoadBalance loadbalance, Invocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {

        if (CollectionUtils.isEmpty(invokers)) {
            return null;
        }
        // 获取调用方法名
        String methodName = invocation == null ? StringUtils.EMPTY : invocation.getMethodName();

        // <dubbo:protocol name="dubbo" sticky="true">
        // 获取 sticky 配置，sticky 表示粘滞连接。所谓粘滞连接是指让服务消费者尽可能的
        // 调用同一个服务提供者，除非该提供者挂了再进行切换
        boolean sticky = invokers.get(0).getUrl()
                .getMethodParameter(methodName, Constants.CLUSTER_STICKY_KEY, Constants.DEFAULT_CLUSTER_STICKY);

        //ignore overloaded method
        // 检测 invokers 列表是否包含 stickyInvoker，如果不包含，
        // 说明 stickyInvoker 代表的服务提供者挂了，此时需要将其置空
        if (stickyInvoker != null && !invokers.contains(stickyInvoker)) {
            stickyInvoker = null;
        }
        //ignore concurrency problem
        if (sticky && stickyInvoker != null && (selected == null || !selected.contains(stickyInvoker))) {
            if (availablecheck && stickyInvoker.isAvailable()) {
                return stickyInvoker;
            }
        }

        // 如果线程走到当前代码处，说明前面的 stickyInvoker 为空，或者不可用。
        // 此时继续调用 doSelect 选择 Invoker
        Invoker<T> invoker = doSelect(loadbalance, invocation, invokers, selected);

        if (sticky) {
            stickyInvoker = invoker;
        }
        return invoker;
    }

    private Invoker<T> doSelect(LoadBalance loadbalance, Invocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected) throws RpcException {

        if (CollectionUtils.isEmpty(invokers)) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        Invoker<T> invoker = loadbalance.select(invokers, getUrl(), invocation);

        //If the `invoker` is in the  `selected` or invoker is unavailable && availablecheck is true, reselect.
        // 进行负载均衡后，如果选出来的已经调用过了，则重新进行负载均衡
        if ((selected != null && selected.contains(invoker))
                || (!invoker.isAvailable() && getUrl() != null && availablecheck)) {
            try {
                // 进行重选
                Invoker<T> rinvoker = reselect(loadbalance, invocation, invokers, selected, availablecheck);
                if (rinvoker != null) {
                    invoker = rinvoker;
                } else {
                    //Check the index of current selected invoker, if it's not the last one, choose the one at index+1.
                    int index = invokers.indexOf(invoker);
                    try {
                        //Avoid collision
                        invoker = invokers.get((index + 1) % invokers.size());
                    } catch (Exception e) {
                        logger.warn(e.getMessage() + " may because invokers list dynamic change, ignore.", e);
                    }
                }
            } catch (Throwable t) {
                logger.error("cluster reselect fail reason is :" + t.getMessage() + " if can not solve, you can set cluster.availablecheck=false in url", t);
            }
        }
        return invoker;
    }

    /**
     * Reselect, use invokers not in `selected` first, if all invokers are in `selected`,
     * just pick an available one using loadbalance policy.
     *
     * @param loadbalance    load balance policy
     * @param invocation     invocation
     * @param invokers       invoker candidates
     * @param selected       exclude selected invokers or not
     * @param availablecheck check invoker available if true
     * @return the reselect result to do invoke
     * @throws RpcException exception
     */
    private Invoker<T> reselect(LoadBalance loadbalance, Invocation invocation,
                                List<Invoker<T>> invokers, List<Invoker<T>> selected, boolean availablecheck) throws RpcException {

        //Allocating one in advance, this list is certain to be used.
        List<Invoker<T>> reselectInvokers = new ArrayList<>(
                invokers.size() > 1 ? (invokers.size() - 1) : invokers.size());

        // First, try picking a invoker not in `selected`.
        for (Invoker<T> invoker : invokers) {
            if (availablecheck && !invoker.isAvailable()) {
                continue;
            }

            if (selected == null || !selected.contains(invoker)) {
                reselectInvokers.add(invoker);
            }
        }

        // 进行一次负载均衡调用之后选到调用过的之后，在重新选择的时候不会包含已经调用过的
        if (!reselectInvokers.isEmpty()) {
            return loadbalance.select(reselectInvokers, getUrl(), invocation);
        }

        // Just pick an available invoker using loadbalance policy
        if (selected != null) {
            for (Invoker<T> invoker : selected) {
                if ((invoker.isAvailable()) // available first
                        && !reselectInvokers.contains(invoker)) {
                    reselectInvokers.add(invoker);
                }
            }
        }
        if (!reselectInvokers.isEmpty()) {
            return loadbalance.select(reselectInvokers, getUrl(), invocation);
        }

        return null;
    }

    @Override
    public Result invoke(final Invocation invocation) throws RpcException {
        checkWhetherDestroyed();

        // binding attachments into invocation.
        Map<String, String> contextAttachments = RpcContext.getContext().getAttachments();
        if (contextAttachments != null && contextAttachments.size() != 0) {
            ((RpcInvocation) invocation).addAttachments(contextAttachments);
        }

        // 经过路由之后的最终的Invokers
        // 这里进行过滤
        List<Invoker<T>> invokers = list(invocation);
        // 初始化负载均衡策略
        LoadBalance loadbalance = initLoadBalance(invokers, invocation);
        RpcUtils.attachInvocationIdIfAsync(getUrl(), invocation);
        // 具体调用过程让子类去实现
        return doInvoke(invocation, invokers, loadbalance);
    }

    protected void checkWhetherDestroyed() {
        if (destroyed.get()) {
            throw new RpcException("Rpc cluster invoker for " + getInterface() + " on consumer " + NetUtils.getLocalHost()
                    + " use dubbo version " + Version.getVersion()
                    + " is now destroyed! Can not invoke any more.");
        }
    }

    @Override
    public String toString() {
        return getInterface() + " -> " + getUrl().toString();
    }

    protected void checkInvokers(List<Invoker<T>> invokers, Invocation invocation) {
        if (CollectionUtils.isEmpty(invokers)) {
            throw new RpcException(RpcException.NO_INVOKER_AVAILABLE_AFTER_FILTER, "Failed to invoke the method "
                    + invocation.getMethodName() + " in the service " + getInterface().getName()
                    + ". No provider available for the service " + directory.getUrl().getServiceKey()
                    + " from registry " + directory.getUrl().getAddress()
                    + " on the consumer " + NetUtils.getLocalHost()
                    + " using the dubbo version " + Version.getVersion()
                    + ". Please check if the providers have been started and registered.");
        }
    }

    protected abstract Result doInvoke(Invocation invocation, List<Invoker<T>> invokers,
                                       LoadBalance loadbalance) throws RpcException;

    // 根据Invocation获取服务提供者
    protected List<Invoker<T>> list(Invocation invocation) throws RpcException {
        // 交给子类的doList去实现
        return directory.list(invocation);
    }

    /**
     * Init LoadBalance.
     * <p>
     * if invokers is not empty, init from the first invoke's url and invocation
     * if invokes is empty, init a default LoadBalance(RandomLoadBalance)
     * </p>
     *
     * @param invokers   invokers
     * @param invocation invocation
     * @return LoadBalance instance. if not need init, return null.
     */
    protected LoadBalance initLoadBalance(List<Invoker<T>> invokers, Invocation invocation) {
        if (CollectionUtils.isNotEmpty(invokers)) {
            // 可以在4种地方配置，优先级如下
            // 1. 客户端方法级别配置。
            // 2. 客户端接口级别配置。
            // 3. 服务端方法级别配置。
            // 4. 服务端接口级别配置。
            return ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(invokers.get(0).getUrl()
                    .getMethodParameter(RpcUtils.getMethodName(invocation), Constants.LOADBALANCE_KEY, Constants.DEFAULT_LOADBALANCE));
        } else {
            // 默认的负载均衡策略为随机
            return ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(Constants.DEFAULT_LOADBALANCE);
        }
    }
}
