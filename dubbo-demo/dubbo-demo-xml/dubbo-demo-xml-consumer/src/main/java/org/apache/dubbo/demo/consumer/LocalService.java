package org.apache.dubbo.demo.consumer;

import org.apache.dubbo.demo.DemoService;

/**
 * @author lilimin
 * @since 2021-06-18
 */
public class LocalService {

    private DemoService demoService;

    public DemoService getDemoService() {
        return demoService;
    }

    public void setDemoService(DemoService demoService) {
        this.demoService = demoService;
    }

    public String sayHello(String name) {
        return demoService.sayHello(name);
    }
}
