package org.apache.dubbo;

import java.util.Iterator;
import java.util.ServiceLoader;

public class JavaSpiDemo {

    public static void main(String[] args) {
        ServiceLoader<Car> carServiceLoader = ServiceLoader.load(Car.class);
        Iterator<Car> it = carServiceLoader.iterator();
        while (it.hasNext()) {
            Car car = it.next();
            // benz
            // bmw
            System.out.println(car.getBrand());
        }
    }
}
