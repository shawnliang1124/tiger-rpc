package com.shawnliang.tiger.core.common;

import com.shawnliang.tiger.core.exception.RpcException;
import com.shawnliang.tiger.core.utils.ConcurrentHashSet;
import com.shawnliang.tiger.core.utils.RemotingUtil;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final ConcurrentHashMap<String/* attr key*/, Object /*attr value*/> attributes       = new ConcurrentHashMap<>();

    private static AtomicInteger atomicInteger = new AtomicInteger(0);

    private  final Set<String> poolKeys = new ConcurrentHashSet<>();

    private AtomicBoolean closed = new AtomicBoolean(false);

    private static final Logger logger = LoggerFactory.getLogger(NettyConnection.class);

    /**
     * todo version的使用
     */
    private Byte  version = new Byte("0");

    private UrlInfo urlInfo;


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

    public Object getAttributes(String attrKey) {
        return attributes.get(attrKey);
    }

    private void init() {
        this.channel.attr(HEARTBEAT_COUNT).set(0);
        this.channel.attr(VERSION).set(this.version);
        this.channel.attr(HEARTBEAT_SWITCH).set(true);
    }

    public boolean isFine() {
        return true;
    }

    public void increaseRefCount() {
        atomicInteger.incrementAndGet();
    }

    public Channel getChannel() {
        return channel;
    }

    public void remove() {

    }

    public Set<String> getPoolKeys() {
        return poolKeys;
    }

    public void close() {
        if (closed.compareAndSet(false, true) ){
            try {
                if (channel != null) {
                    channel.close().addListener((future -> {
                       if (logger.isDebugEnabled()) {
                           logger.debug("close channel result is:[address: {}, successFlag: {}, cause: {}]",
                                   RemotingUtil.parseRemoteAddress(getChannel()), future.isSuccess(), future.cause());
                       }
                    }));
                }
            } catch (Exception e) {
                logger.warn("Exception caught when closing connection {}",
                        RemotingUtil.parseRemoteAddress(getChannel()), e);
            }

        }
    }

    public void decreaseRef() {
        atomicInteger.getAndDecrement();
    }

    public boolean noRef() {
      return atomicInteger.get() == 0;
    }
}
