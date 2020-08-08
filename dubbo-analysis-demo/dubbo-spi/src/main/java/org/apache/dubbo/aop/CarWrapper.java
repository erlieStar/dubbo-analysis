package org.apache.dubbo.aop;

public class CarWrapper implements Car {

    private Car car;

    public CarWrapper(Car car) {
        this.car = car;
    }

    @Override
    public void getBrand() {
        System.out.println("start");
        car.getBrand();
        System.out.println("end");
    }
}
