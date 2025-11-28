package tn.esprit.docsbackend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import tn.esprit.docsbackend.utils.JwtTokenProvider;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketUserInterceptor implements HandshakeInterceptor, ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        // Clients connect like: ws://.../ws?token=JWT
        String query = request.getURI().getQuery();
        if (query != null) {
            // Very simple parsing: token=xxxxx or token=xxxxx&...
            for (String part : query.split("&")) {
                if (part.startsWith("token=")) {
                    String token = part.substring("token=".length());
                    if (jwtTokenProvider.validateToken(token)) {
                        Long userId = jwtTokenProvider.getUserIdFromToken(token);
                        if (userId != null) {
                            attributes.put("userId", userId);
                        }
                    }
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // no-op
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

        if (sessionAttributes != null) {
            Object userId = sessionAttributes.get("userId");
            if (userId != null) {
                accessor.setUser(() -> String.valueOf(userId));
            }
        }

        return message;
    }
}
