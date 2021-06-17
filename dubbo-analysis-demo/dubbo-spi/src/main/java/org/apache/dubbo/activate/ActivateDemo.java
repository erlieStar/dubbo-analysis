package org.apache.dubbo.activate;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;

import java.util.List;

/**
 * @author lilimin
 * @since 2020-08-15
 */
public class ActivateDemo {

    public static void main(String[] args) {
        ExtensionLoader<MyFilter> extensionLoader = ExtensionLoader.getExtensionLoader(MyFilter.class);
        // url中没有参数
        URL url = URL.valueOf("test://localhost");
        List<MyFilter> allFilterList = extensionLoader.getActivateExtension(url, "", null);
        /**
         * org.apache.dubbo.activate.MyConsumerFilter@53e25b76
         * org.apache.dubbo.activate.MyProviderFilter@73a8dfcc
         * org.apache.dubbo.activate.MyLogFilter@ea30797
         *
         * 不指定组则所有的Filter都被激活
         */
        allFilterList.forEach(item -> System.out.println(item));
        System.out.println();

        List<MyFilter> consumerFilterList = extensionLoader.getActivateExtension(url, "", "consumer");
        /**
         * org.apache.dubbo.activate.MyConsumerFilter@53e25b76
         * org.apache.dubbo.activate.MyLogFilter@ea30797
         *
         * 指定consumer组，则只有consumer组的Filter被激活
         */
        consumerFilterList.forEach(item -> System.out.println(item));
        System.out.println();

        // url中有参数myfilter
        url = URL.valueOf("test://localhost?cache=xxx&test=a");
        List<MyFilter> customerFilter = extensionLoader.getActivateExtension(url, "", "consumer");
        /**
         * org.apache.dubbo.activate.MyConsumerFilter@53e25b76
         * org.apache.dubbo.activate.MyLogFilter@ea30797
         * org.apache.dubbo.activate.MyCacheFilter@aec6354
         *
         * 指定key在consumer组的基础上，MyCacheFilter被激活
         */
        customerFilter.forEach(item -> System.out.println(item));
        System.out.println();
    }
}
