package org.apache.dubbo.remoting;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author lilimin
 * @since 2021-06-17
 */
class TransportersTest {

    @Test
    void getTransporter() throws IOException {
        Transporter transporter = Transporters.getTransporter();
        // org.apache.dubbo.remoting.Transporter$Adaptive@49070868
        System.out.println(transporter);
        System.in.read();
    }
}