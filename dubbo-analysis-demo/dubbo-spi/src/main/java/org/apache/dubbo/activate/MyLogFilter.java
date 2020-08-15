package org.apache.dubbo.activate;


import org.apache.dubbo.common.extension.Activate;

/**
 * @author lilimin
 * @since 2020-08-15
 */
@Activate(group = {"consumer", "provider"})
public class MyLogFilter implements MyFilter {
    @Override
    public void filter() {

    }
}
