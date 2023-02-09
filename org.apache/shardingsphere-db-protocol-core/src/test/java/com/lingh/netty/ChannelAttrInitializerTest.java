package com.lingh.netty;

import io.netty.channel.ChannelHandlerContext;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.netty.ChannelAttrInitializer;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class ChannelAttrInitializerTest {
    @Test
    public void assertChannelActive() {
        ChannelHandlerContext context = mock(ChannelHandlerContext.class, RETURNS_DEEP_STUBS);
        new ChannelAttrInitializer().channelActive(context);
        verify(context.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY)).setIfAbsent(any(Charset.class));
        verify(context).fireChannelActive();
    }
}
