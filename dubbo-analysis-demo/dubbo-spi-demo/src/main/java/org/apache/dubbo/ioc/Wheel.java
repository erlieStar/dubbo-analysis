package org.apache.dubbo.ioc;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;

/**
 * @author lilimin
 * @since 2020-06-08
 */
@SPI
public interface Wheel {

    @Adaptive("wheel")
    void getBrandByUrl(URL url);
}
