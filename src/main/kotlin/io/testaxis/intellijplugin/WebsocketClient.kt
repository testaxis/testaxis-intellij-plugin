package io.testaxis.intellijplugin

import io.testaxis.intellijplugin.models.Build
import org.glassfish.tyrus.client.ClientManager
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompFrameHandler
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSession.Subscription
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import org.springframework.web.socket.sockjs.client.SockJsClient
import org.springframework.web.socket.sockjs.client.Transport
import org.springframework.web.socket.sockjs.client.WebSocketTransport
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec

const val WEBSOCKET_URL = "ws://{host}:{port}/{endpoint}"

const val BUILDS_TOPIC = "/topic/builds"

class WebsocketClient(host: String, port: Int, endpoint: String = "/websocket") {
    private val stompSession: StompSession

    init {
        val webSocketTransport: Transport = WebSocketTransport(StandardWebSocketClient(ClientManager.createClient()))

        val sockJsClient = SockJsClient(listOf(webSocketTransport)).apply {
            messageCodec = Jackson2SockJsMessageCodec()
        }

        val stompClient = WebSocketStompClient(sockJsClient).apply {
            messageConverter = MappingJackson2MessageConverter().apply { objectMapper = createObjectMapper() }
        }

        stompSession = stompClient.connect(WEBSOCKET_URL, SessionHandler(), host, port, endpoint).apply {
            addCallback({ }, { exception -> exception.printStackTrace() })
        }.get()
    }

    fun subscribeToBuilds(handler: (Build) -> Unit): Subscription =
        stompSession.subscribe(
            BUILDS_TOPIC,
            object : StompFrameHandler {
                override fun getPayloadType(stompHeaders: StompHeaders) = Build::class.java
                override fun handleFrame(stompHeaders: StompHeaders, build: Any) = handler(build as Build)
            }
        )
}

private class SessionHandler : StompSessionHandlerAdapter() {
    override fun afterConnected(stompSession: StompSession, stompHeaders: StompHeaders) =
        println("Connected to websocket")

    override fun handleException(
        session: StompSession?,
        command: StompCommand?,
        headers: StompHeaders?,
        payload: ByteArray?,
        exception: Throwable?
    ) {
        throw WebsocketException("Failure in WebSocket handling", exception)
    }
}

private class WebsocketException(message: String, exception: Throwable?) : RuntimeException(message, exception)
