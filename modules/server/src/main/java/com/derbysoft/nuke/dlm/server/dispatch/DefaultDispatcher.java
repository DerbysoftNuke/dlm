package com.derbysoft.nuke.dlm.server.dispatch;

import com.google.common.base.Charsets;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 * Created by passyt on 16-9-22.
 */
@Component
public class DefaultDispatcher implements IDispatcher, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(DefaultDispatcher.class);
    private static Properties PROPERTIES = new Properties();

    private ApplicationContext applicationContext;

    static {
        try {
            PROPERTIES.load(DefaultDispatcher.class.getClassLoader().getResourceAsStream("handlers.properties"));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public DefaultFullHttpResponse invoke(FullHttpRequest request) {
        String uri = request.uri();
        HttpMethod method = request.method();

        String handlerName = PROPERTIES.getProperty(uri + "." + method);
        if (handlerName == null) {
            handlerName = PROPERTIES.getProperty(uri);
        }

        log.debug("Process request uri {} and method {} by handler {}", uri, method, handlerName);
        DefaultFullHttpResponse response = null;
        try {
            if (handlerName == null) {
                response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer(notFound(uri).getBytes()));
                response.headers().add("Content-Type", "text/html; charset=utf-8");
            } else {
                IHandler handler = applicationContext.getBean(handlerName, IHandler.class);
                String content = handler.execute(uri, method, request.content().toString(Charset.forName("UTF-8")));

                response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(content.getBytes(Charsets.UTF_8)));
                response.headers().add("Content-Type", handler.getContentType() + "; charset=utf-8");
                response.headers().add("Server", "Netty-5.0");
            }
        } catch (Exception e) {
            log.error("Failed on uri:" + uri + ", method:" + method, e);
            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));

            response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer(writer.toString().getBytes(Charsets.UTF_8)));
            response.headers().add("Content-Type", "text/html; charset=utf-8");
        }

        response.headers().add("Content-Length", String.valueOf(response.content().capacity()));
        response.headers().add("Date", ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME));
        response.headers().add("Server", "Netty-5.0");
        return response;
    }

    protected String notFound(String uri) {
        return new StringBuilder()
                .append("<html>")
                .append("<head>")
                .append("<title>404 Not Found</title>")
                .append("</head>")
                .append("<body>")
                .append("<h1>Not Found</h1>")
                .append("<p>The requested URL ").append(uri).append(" was not found on this server.</p>")
                .append("</body>")
                .append("</html>").toString();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
