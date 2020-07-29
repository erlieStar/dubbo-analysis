package org.apache.dubbo.ioc;

import org.apache.dubbo.common.extension.SPI;

@SPI
public interface Car {

    void getBrandByUrl();
}
