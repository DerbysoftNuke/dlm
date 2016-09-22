package com.derbysoft.nuke.dlm.server.dispatch;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by passyt on 16-9-22.
 */
@Component
public class DefaultDispatcher implements IDispatcher, ApplicationContextAware, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DefaultDispatcher.class);

    private ApplicationContext applicationContext;
    private Map<String, HandlerWrapper> handlers = new HashMap<>();

    @Override
    public DefaultFullHttpResponse invoke(FullHttpRequest request) {
        String uri = request.uri();
        String target = uri;
        if (target.indexOf("?") > 0) {
            target = target.substring(0, target.indexOf("?"));
        }

        HttpMethod method = request.method();
        HandlerWrapper handler = handlers.get(target);
        log.debug("Process request uri {} and method {} by handler {}", uri, method, handler);
        DefaultFullHttpResponse response = null;
        try {
            if (handler == null) {
                response = new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.NOT_FOUND, Unpooled.copiedBuffer(notFound(uri).getBytes()));
                response.headers().add("Content-Type", "text/html; charset=utf-8");
            } else {
                String content = handler.getHandler().execute(uri, method, request.content().toString(Charset.forName("UTF-8")));

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

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(RequestMapping.class);
        for (Object object : beans.values()) {
            if (object instanceof IHandler) {
                IHandler handler = IHandler.class.cast(object);
                RequestMapping requestMapping = handler.getClass().getAnnotation(RequestMapping.class);
                handlers.put(requestMapping.uri(), new HandlerWrapper(handler, requestMapping.contentType()));
            }
        }
    }

    private static class HandlerWrapper {

        private final IHandler handler;
        private final String contentType;

        public HandlerWrapper(IHandler handler, String contentType) {
            this.handler = handler;
            this.contentType = contentType;
        }

        public IHandler getHandler() {
            return handler;
        }

        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HandlerWrapper)) return false;
            HandlerWrapper that = (HandlerWrapper) o;
            return Objects.equal(handler, that.handler) &&
                    Objects.equal(contentType, that.contentType);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(handler, contentType);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("handler", handler)
                    .add("contentType", contentType)
                    .toString();
        }
    }

}
