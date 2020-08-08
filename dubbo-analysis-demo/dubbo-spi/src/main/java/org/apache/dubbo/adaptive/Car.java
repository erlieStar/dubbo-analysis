package org.apache.dubbo.adaptive;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.SPI;

@SPI
public interface Car {

    void getBrandByUrl(URL url);
}
