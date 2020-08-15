package org.apache.dubbo.activate;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.Filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lilimin
 * @since 2020-08-15
 */
public class FilterDemo {

    public static void main(String[] args) {
        ExtensionLoader<Filter> extensionLoader = ExtensionLoader.getExtensionLoader(Filter.class);
        Map<String, String> map = new HashMap<>();
        URL url = new URL("", "", 1, map);
        List<Filter> filterList = extensionLoader.getActivateExtension(url, "", Constants.PROVIDER);
        filterList.forEach(item -> System.out.println(item));
    }
}
