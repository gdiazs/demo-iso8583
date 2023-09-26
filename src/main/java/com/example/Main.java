package com.example;

import java.io.IOException;

import com.github.kpavlov.jreactive8583.iso.ISO8583Version;
import com.github.kpavlov.jreactive8583.iso.J8583MessageFactory;
import com.github.kpavlov.jreactive8583.iso.MessageOrigin;
import com.github.kpavlov.jreactive8583.server.Iso8583Server;
import com.github.kpavlov.jreactive8583.server.ServerConfiguration;
import com.solab.iso8583.parse.ConfigParser;

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


        server.init();

        server.start();
        if (server.isStarted()) {
        }
    }
}