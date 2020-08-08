package org.apache.dubbo.ioc;

import org.apache.dubbo.common.extension.ExtensionLoader;

public class DubboSpiIocDemo {

    /**
     * 这个例子会报异常，说明dubbo必须用url指定扩展点对应的实现类
     * @param args
     */
    public static void main(String[] args) {
        ExtensionLoader<Car> extensionLoader = ExtensionLoader.getExtensionLoader(Car.class);
        Car car = extensionLoader.getExtension("benz");
        // benzCar
        // benzWheel
        car.getBrandByUrl();
    }
}
