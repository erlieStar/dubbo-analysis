package org.apache.dubbo.quickstart;

public class BMWCar implements Car {
    @Override
    public void getBrand() {
        System.out.println("bmw");
    }
}
