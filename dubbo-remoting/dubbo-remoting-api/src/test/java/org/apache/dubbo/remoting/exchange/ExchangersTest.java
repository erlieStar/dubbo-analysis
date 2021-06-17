package org.apache.dubbo.remoting.exchange;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lilimin
 * @since 2021-06-17
 */
class ExchangersTest {

    @Test
    void getExchanger() {
        Exchanger header = Exchangers.getExchanger("header");
        // org.apache.dubbo.remoting.exchange.support.header.HeaderExchanger@1d8d30f7
        System.out.println(header);
    }
}