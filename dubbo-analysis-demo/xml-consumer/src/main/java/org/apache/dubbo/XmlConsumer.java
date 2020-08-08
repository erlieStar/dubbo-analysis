package org.apache.dubbo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class XmlConsumer {
    public static void main( String[] args ) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("consumer.xml");
        EchoService echoService = (EchoService) context.getBean("echoService");
        String message = echoService.echo("hello word");
        System.out.println(message);
    }
}
