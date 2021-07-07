package org.tinygame.herostory;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinygame.herostory.msg.GameMsgProtocol;

/**
 * 消息解码器
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameMsgDecoder.class);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (null == ctx || null == msg) {
            return;
        }
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }

        // WebSocket 二进制消息会通过 HttpServerCodec 解码成 BinaryWebSocketFrame 类对象
        BinaryWebSocketFrame inputFrame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = inputFrame.content();

        byteBuf.readShort();  // 读取消息的长度
        int msgCode = byteBuf.readShort(); // 读取消息的编号

        // 获取消息构建器
        Message.Builder msgBuilder = GameMsgRecognizer.getBuilderByMsgCode(msgCode);
        if (null == msgBuilder) {
            LOGGER.error("无法识别的消息, msgCode = {}", msgCode);
            return;
        }

        // 拿到消息体
        byte[] msgBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msgBody);


        msgBuilder.clear();
        msgBuilder.mergeFrom(msgBody);

        // 构建消息实体
        Message newMsg = msgBuilder.build();


        if (null != newMsg) {
            ctx.fireChannelRead(newMsg);
        }

    }
}
