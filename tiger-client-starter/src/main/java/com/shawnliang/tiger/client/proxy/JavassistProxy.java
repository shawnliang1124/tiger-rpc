package com.shawnliang.tiger.client.proxy;

import com.shawnliang.tiger.client.annonations.CallRpc;
import com.shawnliang.tiger.client.proxy.javaassist.UselessInvocationHandler;
import com.shawnliang.tiger.client.transport.TigerRpcClientTransport;
import com.shawnliang.tiger.client.transport.TransMetaInfo;
import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.proxy.TigerProxy;
import com.shawnliang.tiger.core.spi.TigerSpiImpl;
import com.shawnliang.tiger.core.utils.ReflectUtils;
import com.shawnliang.tiger.core.utils.TigerClassUtils;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/26
 */
@TigerSpiImpl(value = "javassist")
public class JavassistProxy implements TigerProxy {

    private static final Logger logger = LoggerFactory.getLogger(JavassistProxy.class);

    private static final Map<Class<?>, Object> PROXY_CLASS_MAP = new ConcurrentHashMap<>();

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(1);

    private final TransMetaInfo transMetaInfo;

    private final TigerRpcClientTransport clientTransport;

    public JavassistProxy(TransMetaInfo transMetaInfo,
            TigerRpcClientTransport clientTransport) {
        this.transMetaInfo = transMetaInfo;
        this.clientTransport = clientTransport;
    }

    @Override
    public <T> T getProxy(Class<T> interfaceClass) {
        if (!interfaceClass.isInterface()) {
            throw new RpcException(
                    "proxy class is not interface, can't be used in javassist." + interfaceClass
                            .getName());
        }

        Object proxyInstance = PROXY_CLASS_MAP.get(interfaceClass);
        if (proxyInstance != null) {
            return (T) proxyInstance;
        }

        try {
            ClassPool pool = ClassPool.getDefault();
//            pool.appendClassPath(new LoaderClassPath(JavassistProxy.class.getClassLoader()));

            String proxyClassName = genProxyClassName(interfaceClass);
            // 创建代理类
            CtClass ctClass = pool.makeClass(proxyClassName);
            ctClass.addInterface(pool.get(interfaceClass.getName()));

            // 继承反射的java.lang.reflect.Proxy
            ctClass.setSuperclass(pool.get(java.lang.reflect.Proxy.class.getName()));

            // 设置构造方法
            CtClass transMetaClass = pool.get(TransMetaInfo.class.getName());
            CtClass clientTrans = pool.get(TigerRpcClientTransport.class.getName());
//            CtConstructor constructor = new CtConstructor(
//                    new CtClass[]{transMetaClass, clientTrans}, ctClass);
            CtConstructor constructor = new CtConstructor(
                    null, ctClass);
            constructor.setModifiers(Modifier.PUBLIC);
            StringBuilder constructBody = new StringBuilder();
            constructBody
                    .append("{super(new " + UselessInvocationHandler.class.getName() + "()); ");
//            constructBody.append("this.transMetaInfo = transMetaInfo;");
//            constructBody.append("this.clientTransport = clientTransport;");
            constructBody.append("}");
            constructor.setBody(constructBody.toString());

            ctClass.addConstructor(constructor);

            List<String> fieldList = new ArrayList<>();
            List<String> methodList = new ArrayList<>();

            createMethod(interfaceClass, fieldList, methodList);

            // 初始化好属性
            fieldList
                    .add("private com.shawnliang.tiger.client.transport.TransMetaInfo transMetaInfo;");
            fieldList
                    .add("private com.shawnliang.tiger.client.transport.TigerRpcClientTransport clientTransport;");

            for (String fieldStr : fieldList) {
                ctClass.addField(CtField.make(fieldStr, ctClass));
                logger.info("create fieldStr: {} ok", fieldStr);
            }

            for (String methodStr : methodList) {
                ctClass.addMethod(CtMethod.make(methodStr, ctClass));
                logger.info("make methodStr ok, str: [{}]", methodStr);
            }

            Class<?> tmpClazz = ctClass.toClass();
            Object instance = tmpClazz.newInstance();
            Field transMetaInfo = tmpClazz.getDeclaredField("transMetaInfo");
            transMetaInfo.setAccessible(true);
            transMetaInfo.set(instance, this.transMetaInfo);

            Field clientTransport = tmpClazz.getDeclaredField("clientTransport");
            clientTransport.setAccessible(true);
            clientTransport.set(instance, this.clientTransport);

            PROXY_CLASS_MAP.put(interfaceClass, instance);

            return (T) instance;
        } catch (Exception e) {
            logger.error("javassist generate proxy error ", e);
            throw new RpcException("javassist generate proxy error");
        }

    }

    private static String genProxyClassName(Class<?> interfaceClass) {
        return String
                .format("%sProxy%d", interfaceClass.getName(), ATOMIC_INTEGER.getAndIncrement());
    }

    private void createMethod(Class<?> interfaceClass, List<String> fieldList,
            List<String> resultList) {
        Method[] methods = interfaceClass.getMethods();
        StringBuilder stringBuilder = new StringBuilder(1024);
        int methodCount = 0;

        for (Method method : methods) {
            methodCount++;
            if (Modifier.isNative(method.getModifiers()) || Modifier.isFinal(method.getModifiers())
                    ||
                    Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();

            // ex: 重载接口的抽象方法
            // ex: StringBuilder对象如下：
            // public Return对象 抽象方法名 (
            stringBuilder.append(Modifier.toString(method.getModifiers()).replace("abstract", ""))
                    .append(" ").append(TigerClassUtils.getTypeStr(returnType)).append(" ")
                    .append(method.getName()).append("( ");

            // 添加参数内容
            int argNum = 0;
            for (Class<?> parameterType : parameterTypes) {
                stringBuilder.append(" ").append(parameterType.getCanonicalName())
                        .append(" arg").append(argNum).append(" ,");
                argNum++;
            }
            // 删除最后一个参数的逗号
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);

            // 完成方法参数的拼接
            // ex: StringBuilder对象如下：
            // public Return对象 抽象方法名 (参数1 arg0, 参数2 arg2)
            stringBuilder.append(")");

            // 处理方法的异常声明
            Class<?>[] exceptions = method.getExceptionTypes();
            if (exceptions.length > 0) {
                stringBuilder.append(" throws ");
                for (Class<?> exception : exceptions) {
                    stringBuilder.append(exception.getCanonicalName()).append(" ,");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            // 完成异常声明的拼接
            // ex: StringBuilder对象如下：
            // public Return对象 抽象方法名 (参数1 arg0, 参数2 arg2) throws XXException, XX2Exception
            stringBuilder.append(" { ");

            // 开始对抽象方法进行封装
            stringBuilder.append(" Class clazz = ").append(interfaceClass.getCanonicalName())
                    .append(".class;");
            stringBuilder.append(" ").append(Method.class.getCanonicalName())
                    .append(" method = method_")
                    .append(methodCount).append(";");
            stringBuilder.append(" Object[] params = new Object[").append(argNum).append("];");
            stringBuilder.append("Class[] paramTypes = new Class[").append(argNum).append("];");

            StringBuilder methodSig = new StringBuilder();
            for (int i = 0; i < argNum; i++) {
                stringBuilder.append("params[").append(i).append("] = arg").append(i)
                        .append(";");
                stringBuilder.append("paramTypes[").append(i).append("] = ")
                        .append(parameterTypes[i].getCanonicalName()).append(".class")
                        .append(";");
                methodSig.append("," + parameterTypes[i].getCanonicalName() + ".class");
            }

            // 通过反射获取被执行的方法
            fieldList.add("private " + Method.class.getCanonicalName() + " method_" + methodCount
                    + " = "
                    + ReflectUtils.class.getCanonicalName() + ".getMethod("
                    + interfaceClass.getCanonicalName() + ".class, \"" + method.getName() + "\","
                    + (argNum > 0 ? "new Class[]{" + methodSig.toString().substring(1) + "}"
                    : "new Class[0]") + ");"
            );

            // 进行真正rpc请求
            stringBuilder.append(TigerRpcRequest.class.getCanonicalName())
                    .append(" tigerRpcRequest = ")
                    .append("transMetaInfo.getRequest();")
                    .append("tigerRpcRequest.setMethod(method.getName());")
                    .append("tigerRpcRequest.setParams(params);")
                    .append("tigerRpcRequest.setParamsType(paramTypes);");

            stringBuilder.append(CallRpc.class.getCanonicalName());
            stringBuilder.append("  callRpc = method.getAnnotation( ")
                    .append(CallRpc.class.getCanonicalName())
                    .append(".class);");

            stringBuilder.append("Object rtn = null;");
            stringBuilder.append(" if (callRpc == null || callRpc.method().equals(\"sync\"))  {");
            stringBuilder.append(TigerRpcResponse.class.getCanonicalName())
                    .append(" response = ")
                    .append(" clientTransport.sendRequest(transMetaInfo);")
                    .append("if (response == null) {")
                    .append("throw new ")
                    .append(RpcException.class.getCanonicalName())
                    .append("(\"rpc调用结果失败，请求超时：timeout\" + transMetaInfo.getTimeout());")
                    .append("}")
                    .append("rtn = response.getData();")
                    .append("}");

            stringBuilder.append(" else if (callRpc.method().equals(\"async\")) {")
                    .append(" clientTransport.sendRequestAsync(transMetaInfo);")
                    .append("}");

            stringBuilder.append("return rtn ;")
                    .append("}");


            stringBuilder.append("}");
            resultList.add(stringBuilder.toString());
            stringBuilder.delete(0, stringBuilder.length());
        }

    }

    private String asArgument(Class<?> cl, String name) {
        if (cl.isPrimitive()) {
            if (Boolean.TYPE == cl) {
                return name + "==null?false:((Boolean)" + name + ").booleanValue()";
            }
            if (Byte.TYPE == cl) {
                return name + "==null?(byte)0:((Byte)" + name + ").byteValue()";
            }
            if (Character.TYPE == cl) {
                return name + "==null?(char)0:((Character)" + name + ").charValue()";
            }
            if (Double.TYPE == cl) {
                return name + "==null?(double)0:((Double)" + name + ").doubleValue()";
            }
            if (Float.TYPE == cl) {
                return name + "==null?(float)0:((Float)" + name + ").floatValue()";
            }
            if (Integer.TYPE == cl) {
                return name + "==null?(int)0:((Integer)" + name + ").intValue()";
            }
            if (Long.TYPE == cl) {
                return name + "==null?(long)0:((Long)" + name + ").longValue()";
            }
            if (Short.TYPE == cl) {
                return name + "==null?(short)0:((Short)" + name + ").shortValue()";
            }
            throw new RuntimeException(name + " is unknown primitive type.");
        }
        return "(" + TigerClassUtils.getTypeStr(cl) + ")" + name;
    }


}
