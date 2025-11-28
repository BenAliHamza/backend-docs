package tn.esprit.docsbackend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import tn.esprit.docsbackend.security.WebSocketUserInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketUserInterceptor userInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Existing SockJS endpoint (for web)
        registry.addEndpoint("/ws")
                .addInterceptors(userInterceptor)
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Native WebSocket endpoint (for Android)
        registry.addEndpoint("/ws-mobile")
                .addInterceptors(userInterceptor)
                .setAllowedOriginPatterns("*");
        // No withSockJS() here → plain WebSocket + STOMP frames.
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(userInterceptor);
    }
}
