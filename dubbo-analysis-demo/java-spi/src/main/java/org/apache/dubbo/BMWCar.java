package org.apache.dubbo;

public class BMWCar implements Car {
    @Override
    public void getBrand() {
        System.out.println("bmw");
    }
}
