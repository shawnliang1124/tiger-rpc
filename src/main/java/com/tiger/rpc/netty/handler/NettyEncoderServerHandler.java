package com.tiger.rpc.netty.handler;

import com.tiger.rpc.netty.NettyServer;
import com.tiger.rpc.netty.NettyServerInitialize;
import com.tiger.rpc.utils.ProtostuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Description :  netty server encoder .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
public class NettyEncoderServerHandler extends MessageToByteEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] bytes = ProtostuffUtils.serialize(msg);
        byte[] symbolBytes = NettyServerInitialize.SPLIT_SYMBOL.getBytes();

        byte[] total = new byte[bytes.length + symbolBytes.length];

        // 将目标数组内容 复制到total数组中
        System.arraycopy(bytes, 0, total, 0, bytes.length);
        System.arraycopy(symbolBytes, 0 , total, bytes.length, symbolBytes.length);

        // 序列化对象
        out.writeBytes(total);
    }
}
