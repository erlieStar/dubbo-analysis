package org.apache.dubbo;

public class BenzCar implements Car{
    @Override
    public void getBrand() {
        System.out.println("benz");
    }
}
