package org.tinygame.herostory;

import org.tinygame.herostory.mq.MqConsumer;
import org.tinygame.herostory.util.RedisUtil;

public class RankApp {
    public static void main(String[] args) {
        RedisUtil.init();
        MqConsumer.init();
    }
}
