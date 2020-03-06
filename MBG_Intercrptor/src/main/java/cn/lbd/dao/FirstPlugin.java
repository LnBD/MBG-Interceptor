package cn.lbd.dao;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.lang.reflect.Method;
import java.sql.Statement;
import java.util.Properties;
/**
* 编写FirstPlugin插件
 *      步骤：
 *          1.编写Interceptor的实现类
 *          2.使用@Intercepts注解完成该插件签名
 *          3.讲写好的插件注册到SqlMapConfig.xml中，用<plugins>注册插件
 *                注意：由于dtd的约束，属性需要按约定的顺序执行
 *                      顺序（从上到下）：
 *                              properties,settings,typeAliases,typeHandlers,objectFactory,objectWrapperFactory,reflectorFactory,plugins
 *                              ,environments,databaseIdProvider,mappers
 *
 *                              配置示例：
 *                                         <plugins>
 *                                            <plugin interceptor="cn.lbd.dao.FirstPlugin">  这里是全类名
 *                                                <property name="username" value="zhangsan"/>
 *                                                <property name="password" value="123456"/>
 *                                            </plugin>
 *                                            <plugin interceptor="cn.lbd.dao.SecondPlugin">
 *                                                <property name="username" value="zhangsan"/>
 *                                                <property name="password" value="123456"/>
 *                                            </plugin>
 *                                         </plugins>
 *
 *         多个插件会产生多层代理。
 *         创建动态代理时，按照插件配置顺序，创建层层代理对象（12 12 12 12）
 *         执行目标方法时，按逆序执行（21）
* */

//使用@Intercepts注解完成插件签名，告诉MyBatis当前插件用来拦截哪个对象哪个方法
@Intercepts(
        /*@Intercepts的一个属性：Signature[] value();
                @Signature底下有三个属性：
                       Class<?> type()：用于指定四大对象的哪个对象的Class类型,即target目标对象
                            四大对象：StatementHandle,ParameterHandle,ResultSetHandle,Executor
                       String method()：对象中的哪个方法。目标方法执行时会来到代理对象的intercept方法来执行动态代理，增强逻辑
                       Class<?>[] args()：方法中的参数，由于是数组，多值时以{ ， ， }分隔
                            该参数用于出现重载情况时，以args标识究竟是何种方法

                */
        @Signature(type = StatementHandler.class,
                   method = "parameterize",
                   args = {Statement.class})
)
//任何插件都要实现Interceptor接口，复写接口下的三个方法
public class FirstPlugin implements Interceptor {
    @Override
    /*
    拦截目标对象的目标方法的执行
    * */
    public Object intercept(Invocation invocation) throws Throwable {
        //获得被拦截的方法
        Method method = invocation.getMethod();
        System.out.println("被拦截的方法11111111："+method);
        //获取目标对象（要被增强的对象）
        Object target = invocation.getTarget();
        //获取target被代理对象的元数据
        MetaObject metaObject = SystemMetaObject.forObject(target);
        //获取sql语句执行所需要的参数，根据属性名parameterHandler.parameterObject拿到对应属性值1
        //StatementHandler-->ParameterHandler-->parameterObject(sql语句的参数)      依赖关联
        Object value = metaObject.getValue("parameterHandler.parameterObject");
        System.out.println(value);
        //动态修改sql运行参数，从1-->11
        metaObject.setValue("parameterHandler.parameterObject",11);
        //放行。执行目标方法，显式调用。不放行无法实现动态代理。
        //类似于method.invoke(obj,args);
        Object proceed = invocation.proceed();
        return proceed;
    }

    /*
    * 包装目标对象
    *      包装：对目标对象创建一个代理对象
    * */
    @Override
    public Object plugin(Object target) {
        System.out.println("被代理的对象111111111"+target);
        /*
        * 借助Plugin类的wrap方法使用当前Interceptor包装目标对象target
        *       wrap的底层是实质就是通过Proxy.newInstance（）创建一个代理对象的实例，他是MyBatis提供的一个简易的包装方法*/
        Object wrap = Plugin.wrap(target, this);
        //返回当前target创建的动态代理
        return wrap;
    }

    /*
    * 将插件注册时的property属性设置进来
    *
    * 插件配置下的property属性会该方法将这些属性包装成Properties，可供程序运行期间使用
    *       * Properties也是一个集合（key:value）
    */
    @Override
    public void setProperties(Properties properties) {
        System.out.println("插件的配置信息11111"+properties);
    }
}
