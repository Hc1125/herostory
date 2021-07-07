package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.cmdhandler.CmdHandlerFactory;
import org.tinygame.herostory.cmdhandler.ICmdHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主消息处理器
 */
public final class MainMsgProcessor {
    /**
     * 日志对象
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainMsgProcessor.class);

    /**
     * 单例对象
     */
    private static final MainMsgProcessor _instance = new MainMsgProcessor();

    /**
     * 创建一个单线程的线程池
     */
    private final ExecutorService _es = Executors.newSingleThreadExecutor((newRunnable) -> {
        Thread newThread = new Thread(newRunnable);
        newThread.setName("MainMsgProcessor");
        return newThread;
    });

    /**
     * 私有化类默认构造器
     */
    private MainMsgProcessor() {
    }

    /**
     * 获取单例对象
     */
    public static MainMsgProcessor getInstance() {
        return _instance;
    }

    /**
     * 处理客户端消息
     *
     * @param ctx  客户端信道上下文
     * @param msg  消息对象
     */
    public void process(ChannelHandlerContext ctx, Object msg){
        if (null == ctx || null == msg) {
            return;
        }

        final Class<?> msgClazz = msg.getClass();

        LOGGER.info("收到客户端消息，msgClazz = {}, msg = {}", msgClazz.getSimpleName(), msg);

        // 在单线程线程池中运行
        _es.submit(() -> {
            try {
                ICmdHandler<? extends GeneratedMessageV3> cmdHandler = CmdHandlerFactory.create(msg.getClass());
                if (null != cmdHandler) {
                    cmdHandler.handle(ctx, cast(msg));
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        });

    }

    /**
     * 处理 Runnable 实例
     *
     * @param r Runnable
     */
    public void process(Runnable r) {
        if (null == r) {
            return;
        }

        _es.submit(new SafeRun(r));
    }

    /**
     * 转型为命令对象
     *
     * @param msg     消息对象
     * @param <TCmd>  指令类型
     * @return 命令对象
     */
    @SuppressWarnings("unchecked")
    private static <TCmd extends GeneratedMessageV3> TCmd cast(Object msg) {
        if (null == msg) {
            return null;
        } else {
            return (TCmd) msg;
        }
    }

    /**
     * 安全运行
     */
    private static class SafeRun implements Runnable {
        /**
         * 内置运行实例
         */
        private final Runnable _innerR;

        /**
         * 类参数构造器
         */
        SafeRun(Runnable innerR) {
            _innerR = innerR;
        }

        @Override
        public void run() {
            if (null == _innerR) {
                return;
            }

            try {
                // 运行
                _innerR.run();
            } catch (Exception e) {
                // 记录错误日志
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
