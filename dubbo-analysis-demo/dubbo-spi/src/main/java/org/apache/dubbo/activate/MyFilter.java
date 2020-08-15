package org.apache.dubbo.activate;

import org.apache.dubbo.common.extension.SPI;

/**
 * @author lilimin
 * @since 2020-08-15
 */
@SPI
public interface MyFilter {
    void filter();
}
