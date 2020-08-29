
服务端用JavassistProxyFactory将服务包装成一个Invoker对象时会生成一个Wrapper对象，这个Wrapper对象是用代码自动生成的，拼代码的逻辑不再看，用Arthas来看一下最终生成的代码


```shell
curl -O https://alibaba.github.io/arthas/arthas-boot.jar
java -jar arthas-boot.jar
# 根据前面的序号选择进入的进程，然后执行下面的命令
jad *Wrapper*
# 找到org.apache.dubbo.common.bytecode包下的类
# 为org.apache.dubbo.common.bytecode.Wrapper0
# 查看生成的代码
jad org.apache.dubbo.common.bytecode.Wrapper0
```

生成的代码如下

```java
package org.apache.dubbo.common.bytecode;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.apache.dubbo.common.bytecode.ClassGenerator;
import org.apache.dubbo.common.bytecode.NoSuchMethodException;
import org.apache.dubbo.common.bytecode.NoSuchPropertyException;
import org.apache.dubbo.common.bytecode.Wrapper;
import org.apache.dubbo.demo.DemoService;

public class Wrapper0
extends Wrapper
implements ClassGenerator.DC {
    public static String[] pns;
    public static Map pts;
    public static String[] mns;
    public static String[] dmns;
    public static Class[] mts0;

    public String[] getPropertyNames() {
        return pns;
    }

    public Object getPropertyValue(Object object, String string) {
        try {
            DemoService demoService = (DemoService)object;
        }
        catch (Throwable throwable) {
            throw new IllegalArgumentException(throwable);
        }
        throw new NoSuchPropertyException(new StringBuffer().append("Not found property \"").append(string).append("\" field or setter method in class org.apache.dubbo.demo.DemoService.").toString());
    }

    public void setPropertyValue(Object object, String string, Object object2) {
        try {
            DemoService demoService = (DemoService)object;
        }
        catch (Throwable throwable) {
            throw new IllegalArgumentException(throwable);
        }
        throw new NoSuchPropertyException(new StringBuffer().append("Not found property \"").append(string).append("\" field or setter method in class org.apache.dubbo.demo.DemoService.").toString());
    }

    public String[] getMethodNames() {
        return mns;
    }

    public Object invokeMethod(Object object, String string, Class[] arrclass, Object[] arrobject) throws InvocationTargetException {
        DemoService demoService;
        try {
            demoService = (DemoService)object;
        }
        catch (Throwable throwable) {
            throw new IllegalArgumentException(throwable);
        }
        try {
            if ("sayHello".equals(string) && arrclass.length == 1) {
                return demoService.sayHello((String)arrobject[0]);
            }
        }
        catch (Throwable throwable) {
            throw new InvocationTargetException(throwable);
        }
        throw new NoSuchMethodException(new StringBuffer().append("Not found method \"").append(string).append("\" in class org.apache.dubbo.demo.DemoService.").toString());
    }

    public String[] getDeclaredMethodNames() {
        return dmns;
    }

    public Class getPropertyType(String string) {
        return (Class)pts.get(string);
    }

    public boolean hasProperty(String string) {
        return pts.containsKey(string);
    }
}
```
