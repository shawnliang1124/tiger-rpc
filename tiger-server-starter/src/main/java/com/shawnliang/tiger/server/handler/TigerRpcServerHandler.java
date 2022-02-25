package com.shawnliang.tiger.server.handler;

import com.shawnliang.tiger.core.common.ResponseBuilder;
import com.shawnliang.tiger.core.common.TigerRpcRequest;
import com.shawnliang.tiger.core.common.TigerRpcResponse;
import com.shawnliang.tiger.server.store.LocalCacheManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;

/**
 * Description :   .
 *
 * @author : Phoebe
 * @date : Created in 2022/2/13
 */
@Slf4j
public class TigerRpcServerHandler extends SimpleChannelInboundHandler<TigerRpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
            TigerRpcRequest tigerRpcRequest) throws Exception {

        log.info("receive tigerRpcRequest: {}", tigerRpcRequest);
        // 处理请求
        if (tigerRpcRequest != null) {
            // 找到serviceName对应的真正代理类
            String serviceName = tigerRpcRequest.getServiceName();
            Object bean = LocalCacheManager.get(serviceName);

            // 通过反射调用对应的方法
            if (bean == null) {
                throw new RuntimeException("bean not exist!");
            }
            TigerRpcResponse response;
            try {
                Method method = bean.getClass()
                        .getMethod(tigerRpcRequest.getMethod(), tigerRpcRequest.getParamsType());
                Object result = method.invoke(bean, tigerRpcRequest.getParams());

                response = ResponseBuilder.buildSuccess(result);
            } catch (Exception e) {
                log.error("do invoke, reflect method error!", e);
                response = ResponseBuilder
                        .buildFailWithError(e.getCause().toString());
            }

            // 将响应体写回
            tigerRpcRequest.getHeader().setTime(System.currentTimeMillis());
            response.setHeader(tigerRpcRequest.getHeader());
            ctx.writeAndFlush(response);
        }

    }
}
