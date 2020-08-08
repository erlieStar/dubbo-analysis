package org.apache.dubbo.ioc;


public class BenzCar implements Car {

    private Wheel wheel;

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    @Override
    public void getBrandByUrl() {
        System.out.println("benzCar");
        wheel.getBrandByUrl();
    }
}
