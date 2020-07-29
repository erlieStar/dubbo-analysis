package org.apache.dubbo.adaptive;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.ExtensionLoader;

import java.util.HashMap;
import java.util.Map;

public class DubboSpiIocDemo {

    public static void main(String[] args) {
        ExtensionLoader<Car> extensionLoader = ExtensionLoader.getExtensionLoader(Car.class);
        Car car = extensionLoader.getExtension("benz");
        Map<String, String> map = new HashMap<>();
        map.put("wheel", "benz");
        URL url = new URL("", "", 1, map);
        // benzCar
        // benzWheel
        car.getBrandByUrl(url);
    }
}
