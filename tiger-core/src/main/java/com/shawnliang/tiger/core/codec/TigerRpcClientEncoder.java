package com.shawnliang.tiger.core.codec;

import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.serializile.ProtostuffSerialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class TigerRpcClientEncoder extends MessageToByteEncoder<TigerRpcRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, TigerRpcRequest msg, ByteBuf byteBuf)
            throws Exception {
        byte[] bytes = ProtostuffSerialization.serialize(msg);
        byte[] symbolBytes = "$_".getBytes();

        byte[] total = new byte[bytes.length + symbolBytes.length];

        // 将目标数组内容 复制到total数组中
        System.arraycopy(bytes, 0, total, 0, bytes.length);
        System.arraycopy(symbolBytes, 0 , total, bytes.length, symbolBytes.length);

        // 序列化对象
        byteBuf.writeBytes(total);
    }
}
