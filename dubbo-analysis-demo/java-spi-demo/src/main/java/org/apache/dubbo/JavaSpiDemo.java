package org.apache.dubbo;

import java.util.ServiceLoader;

public class JavaSpiDemo {

    public static void main(String[] args) {
        ServiceLoader<Car> carServiceLoader = ServiceLoader.load(Car.class);
        // benz
        // bmw
        carServiceLoader.forEach(Car::getBrand);
    }
}
