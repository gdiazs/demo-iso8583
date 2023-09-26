package com.example;

import java.io.IOException;
import java.time.temporal.IsoFields;

import com.github.kpavlov.jreactive8583.IsoMessageListener;
import com.github.kpavlov.jreactive8583.iso.ISO8583Version;
import com.github.kpavlov.jreactive8583.iso.J8583MessageFactory;
import com.github.kpavlov.jreactive8583.iso.MessageOrigin;
import com.github.kpavlov.jreactive8583.server.Iso8583Server;
import com.github.kpavlov.jreactive8583.server.ServerConfiguration;
import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.IsoValue;
import com.solab.iso8583.parse.ConfigParser;
import com.solab.iso8583.util.HexCodec;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.CharsetUtil;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException {
        var messageFactory = new J8583MessageFactory<>(ConfigParser.createFromClasspathConfig("j8583-config.xml"),
                ISO8583Version.V1987,
                MessageOrigin.ACQUIRER);// [1]

        var config = ServerConfiguration.newBuilder()
                .addEchoMessageListener(true)
                .encodeFrameLengthAsString(true)
                .maxFrameLength(8192)
                .frameLengthFieldAdjust(0).frameLengthFieldOffset(0).frameLengthFieldLength(4)
                .addLoggingHandler(true)
                .build();

        var server = new Iso8583Server<>(5000, config, messageFactory);
        server.addMessageListener(new IsoMessageListener<IsoMessage>() {

            @Override
            public boolean applies(IsoMessage msg) {
                return true;
            }

            @Override
            public boolean onMessage(ChannelHandlerContext ctx, IsoMessage msg) {

                var response = messageFactory.createResponse(msg);
                
                response.setField(39, new IsoValue<String>(IsoType.ALPHA, "05", 2));



                int messageLength = msg.writeData().length;
                byte lengthByte = (byte) messageLength;
                char lenChar = (char) (lengthByte & 0xFF);
                
                var header = String.format("%04d", Integer.toHexString(lenChar));
                response.setIsoHeader(header);

                ctx.writeAndFlush(Unpooled.copiedBuffer(
                        String.format( new String(response.writeData()), ctx.channel().localAddress()), CharsetUtil.US_ASCII));
                return false;
            }

        });

        server.init();

        server.start();
        if (server.isStarted()) {
        }
    }
}