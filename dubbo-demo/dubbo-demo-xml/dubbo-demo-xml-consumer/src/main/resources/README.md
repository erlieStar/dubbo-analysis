
服务端用JavassistProxyFactory将服务包装成一个Invoker对象时会生成一个Wrapper对象，这个Wrapper对象是用代码自动生成的，拼代码的逻辑不再看，用Arthas来看一下最终生成的代码


```shell
curl -O https://alibaba.github.io/arthas/arthas-boot.jar
java -jar arthas-boot.jar
# 根据前面的序号选择进入的进程，然后执行下面的命令
# 查看生成的类
jad *.proxy0
```

生成的代码如下

```java
package org.apache.dubbo.common.bytecode;

import com.alibaba.dubbo.rpc.service.EchoService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.apache.dubbo.common.bytecode.ClassGenerator;
import org.apache.dubbo.demo.DemoService;

public class proxy0
implements ClassGenerator.DC,
EchoService,
DemoService {
    public static Method[] methods;
    private InvocationHandler handler;

    public proxy0(InvocationHandler invocationHandler) {
        this.handler = invocationHandler;
    }

    public proxy0() {
    }

    public Object $echo(Object object) {
        Object[] arrobject = new Object[]{object};
        Object object2 = this.handler.invoke(this, methods[1], arrobject);
        return object2;
    }

    public String sayHello(String string) {
        Object[] arrobject = new Object[]{string};
        Object object = this.handler.invoke(this, methods[0], arrobject);
        return (String)object;
    }
}
```
