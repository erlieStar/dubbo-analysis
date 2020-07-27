package org.apache.dubbo.quickstart;

import org.apache.dubbo.common.extension.SPI;

@SPI
public interface Car {

    void getBrand();
}
