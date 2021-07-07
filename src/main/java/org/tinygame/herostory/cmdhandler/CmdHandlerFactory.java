package org.tinygame.herostory.cmdhandler;

import com.google.protobuf.GeneratedMessageV3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.util.PackageUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 命令处理器工厂类
 */
public final class CmdHandlerFactory {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CmdHandlerFactory.class);

    /**
     * 命令处理器字典
     */
    private static final Map<Class<?>, ICmdHandler<? extends GeneratedMessageV3>> _handlerMap = new HashMap<>();

    /**
     * 私有化类默认构造器
     */
    private CmdHandlerFactory() {}

    /**
     * 初始化
     */
    public static void init() {
        LOGGER.info("==== 完成命令与处理器的关联 ====");
        // 获取包名称
        final String packageName = CmdHandlerFactory.class.getPackage().getName();
        // 获取 ICmdHandler 所有的实现类
        Set<Class<?>> classSet = PackageUtil.listSubClazz(packageName, true, ICmdHandler.class);

        for (Class<?> handlerClazz : classSet) {
            if (null == handlerClazz ||
                    0 != (handlerClazz.getModifiers() & Modifier.ABSTRACT)) {
                continue;
            }

            // 获取方法数组
            Method[] methodArray = handlerClazz.getDeclaredMethods();
            // 消息类型
            Class<?> msgClazz = null;

            for (Method currMethod : methodArray) {
                if (null == currMethod ||
                !currMethod.getName().equals("handle")) {
                    continue;
                }

                // 获取函数参数类型数组
                Class<?>[] paramTypeArray = currMethod.getParameterTypes();

                if (paramTypeArray.length < 2 ||
                        paramTypeArray[1] == GeneratedMessageV3.class ||
                        !GeneratedMessageV3.class.isAssignableFrom(paramTypeArray[1])) {
                    continue;
                }

                msgClazz = paramTypeArray[1];
                break;
            }

            if (null == msgClazz) {
                continue;
            }

            try {
                // 创建命令处理器实例
                ICmdHandler<?> newHandler = (ICmdHandler<?>) handlerClazz.newInstance();

                LOGGER.info("{} <==> {}", msgClazz.getName(), handlerClazz.getSimpleName());
                _handlerMap.put(msgClazz, newHandler);
            } catch (Exception e) {
                // 记录错误日志
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 创建命令处理器
     *
     * @param msgClazz
     * @return
     */
    public static ICmdHandler<? extends GeneratedMessageV3> create(Class<?> msgClazz) {
        if (null == msgClazz) {
            return null;
        }
        return _handlerMap.get(msgClazz);
    }
}
