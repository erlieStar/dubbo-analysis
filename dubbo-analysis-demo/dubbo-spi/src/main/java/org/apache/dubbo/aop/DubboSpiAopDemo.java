package org.apache.dubbo.aop;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class DubboSpiAopDemo {

    public static void main(String[] args) {
        ExtensionLoader<Car> extensionLoader = ExtensionLoader.getExtensionLoader(Car.class);
        Car car = extensionLoader.getExtension("benz");
        // start
        // benz
        // end
        car.getBrand();
    }
}
