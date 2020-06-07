package org.apache.dubbo.ioc;

import org.apache.dubbo.common.URL;

public class BenzCar implements Car {

    // 这个里面存的是代理对象
    private Car car;

    public void setCar(Car car) {
        this.car = car;
    }

    @Override
    public void getBrandByUrl(URL url) {
        System.out.println("benz");
        // 代理类根据URL找到实现类，然后再调用实现类
        car.getBrandByUrl(url);
    }
}
