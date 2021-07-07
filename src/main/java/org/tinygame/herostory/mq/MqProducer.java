package org.tinygame.herostory.mq;

import com.alibaba.fastjson.JSONObject;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息队列生产者
 */
public final class MqProducer {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MqProducer.class);

    /**
     * 消息队列生产者
     */
    private static DefaultMQProducer _producer = null;

    /**
     * 私有化类默认构造器
     */
    private MqProducer() {}

    /**
     * 初始化
     */
    public static void init() {
        try {
            // 创建生产者
            DefaultMQProducer producer = new DefaultMQProducer("herostory");
            // 指定 nameServer 地址
            producer.setNamesrvAddr("192.168.144.201:9876");
            // 启动生产者
            producer.start();
            producer.setRetryTimesWhenSendAsyncFailed(3);

            _producer = producer;

            LOGGER.info("消息队列（ 生产者 ）连接成功！");
        } catch (Exception e) {
            // 记录错误日志
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 发送信息
     *
     * @param topic 主题
     * @param msg 消息对象
     */
    public static void sendMsg(String topic, Object msg) {
        if (null == topic || null == msg) {
            return;
        }

        if (null == _producer) {
            throw new RuntimeException("_producer 尚未初始化");
        }

        Message newMsg = new Message();
        newMsg.setTopic(topic);
        newMsg.setBody(JSONObject.toJSONBytes(msg));

        try {
            // 发送消息
            _producer.send(newMsg);
        } catch (Exception e) {
            // 记录错误日志
            LOGGER.error(e.getMessage(), e);
        }
    }
}
