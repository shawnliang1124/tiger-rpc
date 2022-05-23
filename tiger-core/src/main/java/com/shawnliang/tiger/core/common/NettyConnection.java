package com.shawnliang.tiger.core.common;

import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.utils.ConcurrentHashSet;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Set;

/**
 * Description :   netty channel的抽象连接.
 *
 * @author : Phoebe
 * @date : Created in 2022/5/23
 */
public class NettyConnection {

    private Channel channel;

    public static final AttributeKey<NettyConnection> CONNECTION = AttributeKey
            .valueOf("connection");

    public static final AttributeKey<Integer>     HEARTBEAT_COUNT  = AttributeKey
            .valueOf("heartbeatCount");

    /**
     * todo version的使用
     */
    private Byte  version = new Byte("0");

    private UrlInfo urlInfo;

    private Set<String> poolKeys = new ConcurrentHashSet<>();

    public static final AttributeKey<Byte>  VERSION = AttributeKey
            .valueOf("version");

    public static final AttributeKey<Boolean>  HEARTBEAT_SWITCH = AttributeKey
            .valueOf("heartBeatSwitch");


    public NettyConnection(Channel channel) {
        this.channel = channel;
        // todo 这是干嘛的？
        this.channel.attr(CONNECTION).set(this);
    }

    public NettyConnection(UrlInfo urlInfo, Channel channel) {
        this(channel);

        if (urlInfo == null) {
            throw new RpcException("generate connection error! cause url info is null");
        }

        this.urlInfo = urlInfo;
        this.poolKeys.add(urlInfo.getUniKey());

    }

    private void init() {
        this.channel.attr(HEARTBEAT_COUNT).set(0);
        this.channel.attr(VERSION).set(this.version);
        this.channel.attr(HEARTBEAT_SWITCH).set(true);
    }



}
