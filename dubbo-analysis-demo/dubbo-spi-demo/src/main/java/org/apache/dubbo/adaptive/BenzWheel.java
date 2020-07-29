package org.apache.dubbo.adaptive;

import org.apache.dubbo.common.URL;

/**
 * @author lilimin
 * @since 2020-06-08
 */
public class BenzWheel implements Wheel {

    @Override
    public void getBrandByUrl(URL url) {
        System.out.println("benzWheel");
    }
}
