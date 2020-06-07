package org.apache.dubbo.aop;

public class BenzCar implements Car {
    @Override
    public void getBrand() {
        System.out.println("benz");
    }
}
