package org.apache.dubbo.quickStart;

import org.apache.dubbo.common.extension.SPI;

@SPI
public interface Car {

    void getBrand();
}
