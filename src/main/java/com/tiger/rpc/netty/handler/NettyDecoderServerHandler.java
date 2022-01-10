package com.tiger.rpc.netty.handler;

import com.tiger.rpc.info.RpcResp;
import com.tiger.rpc.utils.ProtostuffUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/1/9
 */
public class NettyDecoderServerHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)
            throws Exception {
        byte[] data = new byte[in.readableBytes()];
        // 将Bytebuf字节流的内容读到data 字节数组中
        in.readBytes(data);

        out.add(ProtostuffUtils.deserialize(data, RpcResp.class));
    }
}
