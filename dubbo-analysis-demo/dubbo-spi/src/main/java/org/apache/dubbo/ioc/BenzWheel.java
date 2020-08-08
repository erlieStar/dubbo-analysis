package org.apache.dubbo.ioc;

/**
 * @author lilimin
 * @since 2020-06-08
 */
public class BenzWheel implements Wheel {

    @Override
    public void getBrandByUrl() {
        System.out.println("benzWheel");
    }
}
