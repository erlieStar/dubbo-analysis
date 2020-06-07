package org.apache.dubbo.quickStart;

public class BenzCar implements Car {

    private Car car;

    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public void getBrand() {
        System.out.println("benz");
    }
}
