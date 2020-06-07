package org.apache.dubbo.ioc;

import org.apache.dubbo.common.URL;

public class BMWCar implements Car {

    @Override
    public void getBrandByUrl(URL url) {
        System.out.println("bmw");
    }
}
