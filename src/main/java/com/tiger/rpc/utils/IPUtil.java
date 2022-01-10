package com.tiger.rpc.utils;

import io.netty.channel.ChannelHandlerContext;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shawnLiang
 * @date 2022.1.9
 */
public class IPUtil {
    public static String getIpV4() {
        String ip = "";
        Enumeration<NetworkInterface> networkInterface;
        try {
            networkInterface = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
             return ip;
        }
        Set<String> ips = new HashSet<String> ();
        while (networkInterface.hasMoreElements()) {
            NetworkInterface ni = networkInterface.nextElement();

            Enumeration<InetAddress> inetAddress = null;
            try {
                if (null != ni) {
                    inetAddress = ni.getInetAddresses();
                }
            } catch (Exception e) {
             }
            while (null != inetAddress && inetAddress.hasMoreElements()) {
                InetAddress ia = inetAddress.nextElement();
                if (ia instanceof Inet6Address) {
                    continue; // ignore ipv6
                }
                String thisIp = ia.getHostAddress();
                // 排除 回送地址
                if (!ia.isLoopbackAddress() && !thisIp.contains(":") && !"127.0.0.1".equals(thisIp)) {
                    ips.add(thisIp);
                    if (StringUtils.isEmpty (ip)) {
                        ip = thisIp;
                    }
                }
            }
        }

        if (StringUtils.isEmpty (ip)) {
             ip = "127.0.0.1";
        }
        return ip;
    }

    public static String getClientIP(ChannelHandlerContext ctx) {
        String ip;
        try {
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            ip = socketAddress.getAddress().getHostAddress();
        } catch (Exception e) {
            ip = "unknown";
        }
        return ip;
    }
}
