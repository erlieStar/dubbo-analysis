package org.apache.dubbo.quickstart;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class DubboSpiDemo {

    public static void main(String[] args) {
        ExtensionLoader<Car> extensionLoader = ExtensionLoader.getExtensionLoader(Car.class);
        Car car = extensionLoader.getExtension("benz");
        car.getBrand();
    }
}
