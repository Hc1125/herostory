package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息识别器
 */
public final class GameMsgRecognizer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgRecognizer.class);

    /**
     * 消息编号 -> 消息体字典
     */
    private static final Map<Integer, GeneratedMessageV3> _msgCodeAndMsgBodyMap = new HashMap<>();

    /**
     * 消息类型 -> 消息编号字典
     */
    private static final Map<Class<?>, Integer> _msgClazzAndMsgCodeMap = new HashMap<>();

    /**
     * 私有化类默认构造器
     */
    private GameMsgRecognizer() {}

    /**
     * 初始化
     */
    public static void init() {
        LOGGER.info("==== 完成消息体与消息编号的关联 ====");

        // 获取所有内部类
        Class<?>[] innerClazzArray = GameMsgProtocol.class.getDeclaredClasses();

        for (Class<?> innerClazz : innerClazzArray) {
            if (null == innerClazz ||
                    !GeneratedMessageV3.class.isAssignableFrom(innerClazz)) {
                // 如果不是消息类
                continue;
            }

            // 获取类名称并转成小写
            String clazzName = innerClazz.getSimpleName();
            clazzName = clazzName.toLowerCase();

            // 接下来遍历 MsgCode 枚举
            for (GameMsgProtocol.MsgCode msgCode : GameMsgProtocol.MsgCode.values()) {
                if (null == msgCode) {
                    continue;
                }

                // 获取消息编码
                // 获取枚举名称并转成小写
                String strMsgCode = msgCode.name();
                strMsgCode = strMsgCode.replaceAll("_", "");
                strMsgCode = strMsgCode.toLowerCase();

                if (!strMsgCode.startsWith(clazzName)) {
                    continue;
                }

                // 消息编号名称和类名称正好相同,
                // 则建立关系

                try {
                    // 调用 XxxCmd 或者 XxxResult 的 getDefaultInstance 静态方法,
                    // 目的是返回默认实例
                    Object returnObj = innerClazz.getDeclaredMethod("getDefaultInstance").invoke(innerClazz);

                    LOGGER.info("关联 {} <==> {}", innerClazz.getName(), msgCode.getNumber());

                    // 关联消息编号与消息体

                    _msgCodeAndMsgBodyMap.put(msgCode.getNumber(), (GeneratedMessageV3) returnObj);

                    // 关联消息类与消息编号
                    _msgClazzAndMsgCodeMap.put(innerClazz, msgCode.getNumber());
                } catch (Exception e) {
                    // 记录错误日志
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 根据消息编号获取消息构建器
     *
     * @param msgCode
     * @return
     */
    public static Message.Builder getBuilderByMsgCode(int msgCode) {
        if (msgCode < 0) {
            return null;
        }

        // 获取消息对象
        GeneratedMessageV3 defaultMsg = _msgCodeAndMsgBodyMap.get(msgCode);

        if (null == defaultMsg) {
            return null;
        }

        return defaultMsg.newBuilderForType();
    }

    /**
     * 根据消息类获取消息编号
     *
     * @param msgClazz
     * @return
     */
    public static int getMsgCodeByClazz(Class<?> msgClazz) {
        if (null == msgClazz) {
            return -1;
        }
        Integer msgCode = _msgClazzAndMsgCodeMap.get(msgClazz);

        if (null == msgCode) {
            return -1;
        } else {
            return msgCode;
        }
    }
}
