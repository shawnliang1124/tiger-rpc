package com.shawnliang.tiger.core.connections;

import io.netty.channel.ChannelHandler;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/6/12
 */
public class DefaultConnectionFactory extends AbsConnectionFactory {

    public DefaultConnectionFactory(ChannelHandler heartBeatHandler,
            ChannelHandler handler, ChannelHandler encoder,
            ChannelHandler decoder, ConnectSelectStrategy selectStrategy) {
        super(heartBeatHandler, handler, encoder, decoder, selectStrategy);
    }
}
