package org.apache.dubbo.ioc;

import org.apache.dubbo.common.extension.SPI;

/**
 * @author lilimin
 * @since 2020-06-08
 */
@SPI
public interface Wheel {

    void getBrandByUrl();
}
