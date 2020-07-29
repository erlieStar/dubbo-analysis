package org.apache.dubbo.ioc;


public class BenzCar implements Car {

    // 这个里面存的是代理对象
    private Wheel wheel;

    public void setWheel(Wheel wheel) {
        this.wheel = wheel;
    }

    @Override
    public void getBrandByUrl() {
        System.out.println("benzCar");
        // 代理类根据URL找到实现类，然后再调用实现类
        wheel.getBrandByUrl();
    }
}
