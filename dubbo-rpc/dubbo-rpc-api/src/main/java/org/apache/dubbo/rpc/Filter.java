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
package org.apache.dubbo.rpc;

import org.apache.dubbo.common.extension.SPI;

/**
 * Extension for intercepting the invocation for both service provider and consumer, furthermore, most of
 * functions in dubbo are implemented base on the same mechanism. Since every time when remote method is
 * invoked, the filter extensions will be executed too, the corresponding penalty should be considered before
 * more filters are added.
 * <pre>
 *  They way filter work from sequence point of view is
 *    <b>
 *    ...code before filter ...
 *          invoker.invoke(invocation) //filter work in a filter implementation class
 *          ...code after filter ...
 *    </b>
 *    Caching is implemented in dubbo using filter approach. If cache is configured for invocation then before
 *    remote call configured caching type's (e.g. Thread Local, JCache etc) implementation invoke method gets called.
 * </pre>
 * Filter. (SPI, Singleton, ThreadSafe)
 *
 * @see org.apache.dubbo.rpc.filter.GenericFilter
 * @see org.apache.dubbo.rpc.filter.EchoFilter
 * @see org.apache.dubbo.rpc.filter.TokenFilter
 * @see org.apache.dubbo.rpc.filter.TpsLimitFilter
 */
@SPI
public interface Filter {

    /**
     * do invoke filter.
     * <p>
     * <code>
     * // before filter
     * Result result = invoker.invoke(invocation);
     * // after filter
     * return result;
     * </code>
     *
     * @param invoker    service
     * @param invocation invocation.
     * @return invoke result.
     * @throws RpcException
     * @see org.apache.dubbo.rpc.Invoker#invoke(Invocation)
     */

    /**
     * 1. 用户自定义 filter 默认在内置 filter 之后。
     * 2. 特殊值 default，表示缺省扩展点插入的位置。比如：filter="xxx,default,yyy"，表示 xxx 在缺省 filter 之前，yyy 在缺省 filter 之后。
     * 3. 特殊符号 -，表示剔除。比如：filter="-foo1"，剔除添加缺省扩展点 foo1。比如：filter="-default"，剔除添加所有缺省扩展点。
     * 4. provider 和 service 同时配置的 filter 时，累加所有 filter，而不是覆盖。
     * 比如：<dubbo:provider filter="xxx,yyy"/> 和 <dubbo:service filter="aaa,bbb" />，
     * 则 xxx,yyy,aaa,bbb 均会生效。如果要覆盖，需配置：<dubbo:service filter="-xxx,-yyy,aaa,bbb" />
     */


    /**
     * <!-- 消费方调用过程拦截 -->
     * <dubbo:reference filter="xxx,yyy" />
     * <!-- 消费方调用过程缺省拦截器，将拦截所有reference -->
     * <dubbo:consumer filter="xxx,yyy"/>
     * <!-- 提供方调用过程拦截 -->
     * <dubbo:service filter="xxx,yyy" />
     * <!-- 提供方调用过程缺省拦截器，将拦截所有service -->
     * <dubbo:provider filter="xxx,yyy"/>
     */
    Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException;

    /**
     * java8新特性，接口默认返回收到的结果
     */
    default Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }

}