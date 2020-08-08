package org.apache.dubbo;

/**
 * @author lilimin
 * @since 2020-08-08
 */
public class EchoServiceImpl implements EchoService {
    @Override
    public String echo(String message) {
        return message;
    }
}
