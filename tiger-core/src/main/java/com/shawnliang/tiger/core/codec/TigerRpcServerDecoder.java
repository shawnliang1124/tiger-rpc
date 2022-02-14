package com.shawnliang.tiger.core.codec;

import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.serializile.ProtostuffSerialization;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
public class TigerRpcServerDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in,
            List<Object> list) throws Exception {
        byte[] data = new byte[in.readableBytes()];
        // 将Bytebuf字节流的内容读到data 字节数组中
        in.readBytes(data);

        list.add(ProtostuffSerialization.deserialize(data, TigerRpcRequest.class));
    }
}
