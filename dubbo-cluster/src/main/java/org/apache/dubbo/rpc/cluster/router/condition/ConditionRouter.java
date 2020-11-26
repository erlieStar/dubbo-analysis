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
package org.apache.dubbo.rpc.cluster.router.condition;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.common.utils.UrlUtils;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.router.AbstractRouter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ConditionRouter
 *
 * 1. scope表示路由规则的作用粒度，scope的取值会决定key的取值。必填。
 *    service 服务粒度
 *    application 应用粒度
 * 2. Key明确规则体作用在哪个服务或应用。必填。
 *    scope=service时，key取值为[{group}:]{service}[:{version}]的组合
 *    scope=application时，key取值为application名称
 * 3. enabled=true 当前路由规则是否生效，可不填，缺省生效。
 * 4. force=false 当路由结果为空时，是否强制执行，如果不强制执行，路由结果为空的路由规则将自动失效，可不填，缺省为 false。
 * 5. runtime=false 是否在每次调用时执行路由规则，否则只在提供者地址列表变更时预先执行并缓存结果，调用时直接从缓存中获取路由结果。如果用了参数路由，必须设为 true，需要注意设置会影响调用的性能，可不填，缺省为 false。
 * 6. priority=1 路由规则的优先级，用于排序，优先级越大越靠前执行，可不填，缺省为 0。
 * 7. conditions 定义具体的路由规则内容。必填。
 *
 */
public class ConditionRouter extends AbstractRouter {
    public static final String NAME = "condition";

    private static final Logger logger = LoggerFactory.getLogger(ConditionRouter.class);
    protected static Pattern ROUTE_PATTERN = Pattern.compile("([&!=,]*)\\s*([^&!=,\\s]+)");
    protected Map<String, MatchPair> whenCondition;
    protected Map<String, MatchPair> thenCondition;

    private boolean enabled;

    public ConditionRouter(String rule, boolean force, boolean enabled) {
        this.force = force;
        this.enabled = enabled;
        this.init(rule);
    }

    public ConditionRouter(URL url) {
        this.url = url;
        // 优先级
        this.priority = url.getParameter(Constants.PRIORITY_KEY, 0);
        // 是否强制执行
        this.force = url.getParameter(Constants.FORCE_KEY, false);
        // 是否生效
        this.enabled = url.getParameter(Constants.ENABLED_KEY, true);
        init(url.getParameterAndDecoded(Constants.RULE_KEY));
    }

    public void init(String rule) {
        try {
            if (rule == null || rule.trim().length() == 0) {
                throw new IllegalArgumentException("Illegal route rule!");
            }
            rule = rule.replace("consumer.", "").replace("provider.", "");
            int i = rule.indexOf("=>");
            String whenRule = i < 0 ? null : rule.substring(0, i).trim();
            String thenRule = i < 0 ? rule.trim() : rule.substring(i + 2).trim();
            // 解析服务消费者匹配规则
            Map<String, MatchPair> when = StringUtils.isBlank(whenRule) || "true".equals(whenRule) ? new HashMap<String, MatchPair>() : parseRule(whenRule);
            // 解析服务提供者匹配规则
            Map<String, MatchPair> then = StringUtils.isBlank(thenRule) || "false".equals(thenRule) ? null : parseRule(thenRule);
            // NOTE: It should be determined on the business level whether the `When condition` can be empty or not.
            this.whenCondition = when;
            this.thenCondition = then;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static Map<String, MatchPair> parseRule(String rule)
            throws ParseException {
        Map<String, MatchPair> condition = new HashMap<String, MatchPair>();
        if (StringUtils.isBlank(rule)) {
            return condition;
        }
        // Key-Value pair, stores both match and mismatch conditions
        MatchPair pair = null;
        // Multiple values
        Set<String> values = null;
        final Matcher matcher = ROUTE_PATTERN.matcher(rule);
        while (matcher.find()) { // Try to match one by one
            String separator = matcher.group(1);
            String content = matcher.group(2);
            // Start part of the condition expression.
            if (StringUtils.isEmpty(separator)) {
                pair = new MatchPair();
                condition.put(content, pair);
            }
            // The KV part of the condition expression
            else if ("&".equals(separator)) {
                if (condition.get(content) == null) {
                    pair = new MatchPair();
                    condition.put(content, pair);
                } else {
                    pair = condition.get(content);
                }
            }
            // The Value in the KV part.
            else if ("=".equals(separator)) {
                if (pair == null) {
                    throw new ParseException("Illegal route rule \""
                            + rule + "\", The error char '" + separator
                            + "' at index " + matcher.start() + " before \""
                            + content + "\".", matcher.start());
                }

                values = pair.matches;
                values.add(content);
            }
            // The Value in the KV part.
            else if ("!=".equals(separator)) {
                if (pair == null) {
                    throw new ParseException("Illegal route rule \""
                            + rule + "\", The error char '" + separator
                            + "' at index " + matcher.start() + " before \""
                            + content + "\".", matcher.start());
                }

                values = pair.mismatches;
                values.add(content);
            }
            // The Value in the KV part, if Value have more than one items.
            else if (",".equals(separator)) { // Should be separated by ','
                if (values == null || values.isEmpty()) {
                    throw new ParseException("Illegal route rule \""
                            + rule + "\", The error char '" + separator
                            + "' at index " + matcher.start() + " before \""
                            + content + "\".", matcher.start());
                }
                values.add(content);
            } else {
                throw new ParseException("Illegal route rule \"" + rule
                        + "\", The error char '" + separator + "' at index "
                        + matcher.start() + " before \"" + content + "\".", matcher.start());
            }
        }
        return condition;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation)
            throws RpcException {
        // 不生效
        if (!enabled) {
            return invokers;
        }

        if (CollectionUtils.isEmpty(invokers)) {
            return invokers;
        }
        try {
            // 没有whenRule匹配，返回所有
            if (!matchWhen(url, invocation)) {
                return invokers;
            }
            List<Invoker<T>> result = new ArrayList<Invoker<T>>();
            // 没有thenRule，则表明服务消费者在黑名单中，返回空列表
            if (thenCondition == null) {
                logger.warn("The current consumer in the service blacklist. consumer: " + NetUtils.getLocalHost() + ", service: " + url.getServiceKey());
                return result;
            }
            for (Invoker<T> invoker : invokers) {
                // 匹配成功
                if (matchThen(invoker.getUrl(), url)) {
                    result.add(invoker);
                }
            }
            if (!result.isEmpty()) {
                // result不为空，直接返回
                return result;
            } else if (force) {
                // result为空 force=true 强制返回空列表
                logger.warn("The route result is empty and force execute. consumer: " + NetUtils.getLocalHost() + ", service: " + url.getServiceKey() + ", router: " + url.getParameterAndDecoded(Constants.RULE_KEY));
                return result;
            }
        } catch (Throwable t) {
            logger.error("Failed to execute condition router rule: " + getUrl() + ", invokers: " + invokers + ", cause: " + t.getMessage(), t);
        }
        // result为空，force=false 返回所有Invoker列表
        return invokers;
    }

    @Override
    public boolean isRuntime() {
        // We always return true for previously defined Router, that is, old Router doesn't support cache anymore.
//        return true;
        return this.url.getParameter(Constants.RUNTIME_KEY, false);
    }

    @Override
    public URL getUrl() {
        return url;
    }

    boolean matchWhen(URL url, Invocation invocation) {
        return CollectionUtils.isEmptyMap(whenCondition) || matchCondition(whenCondition, url, null, invocation);
    }

    private boolean matchThen(URL url, URL param) {
        return CollectionUtils.isNotEmptyMap(thenCondition) && matchCondition(thenCondition, url, param, null);
    }

    private boolean matchCondition(Map<String, MatchPair> condition, URL url, URL param, Invocation invocation) {
        Map<String, String> sample = url.toMap();
        boolean result = false;
        for (Map.Entry<String, MatchPair> matchPair : condition.entrySet()) {
            String key = matchPair.getKey();
            String sampleValue;
            //get real invoked method name from invocation
            if (invocation != null && (Constants.METHOD_KEY.equals(key) || Constants.METHODS_KEY.equals(key))) {
                sampleValue = invocation.getMethodName();
            } else if (Constants.ADDRESS_KEY.equals(key)) {
                sampleValue = url.getAddress();
            } else if (Constants.HOST_KEY.equals(key)) {
                sampleValue = url.getHost();
            } else {
                sampleValue = sample.get(key);
                if (sampleValue == null) {
                    sampleValue = sample.get(Constants.DEFAULT_KEY_PREFIX + key);
                }
            }
            if (sampleValue != null) {
                if (!matchPair.getValue().isMatch(sampleValue, param)) {
                    return false;
                } else {
                    result = true;
                }
            } else {
                //not pass the condition
                if (!matchPair.getValue().matches.isEmpty()) {
                    return false;
                } else {
                    result = true;
                }
            }
        }
        return result;
    }

    protected static final class MatchPair {
        final Set<String> matches = new HashSet<String>();
        final Set<String> mismatches = new HashSet<String>();

        private boolean isMatch(String value, URL param) {
            if (!matches.isEmpty() && mismatches.isEmpty()) {
                for (String match : matches) {
                    if (UrlUtils.isMatchGlobPattern(match, value, param)) {
                        return true;
                    }
                }
                return false;
            }

            if (!mismatches.isEmpty() && matches.isEmpty()) {
                for (String mismatch : mismatches) {
                    if (UrlUtils.isMatchGlobPattern(mismatch, value, param)) {
                        return false;
                    }
                }
                return true;
            }

            if (!matches.isEmpty() && !mismatches.isEmpty()) {
                //when both mismatches and matches contain the same value, then using mismatches first
                for (String mismatch : mismatches) {
                    if (UrlUtils.isMatchGlobPattern(mismatch, value, param)) {
                        return false;
                    }
                }
                for (String match : matches) {
                    if (UrlUtils.isMatchGlobPattern(match, value, param)) {
                        return true;
                    }
                }
                return false;
            }
            return false;
        }
    }
}
