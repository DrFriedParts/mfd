import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.*
import spark.Spark.*
import java.util.concurrent.atomic.AtomicLong

class User(val id: Long, val name: String)
class Message(val msgType: String, val data: Any)

@WebSocket
class ServerRealtime {

    val users = HashMap<Session, User>()
    var uids = AtomicLong(0)

    @OnWebSocketConnect
    fun connected(session: Session) = println("session connected")

    @OnWebSocketMessage
    fun message(session: Session, message: String) {
        val json = ObjectMapper().readTree(message)
        when (json.get("type").asText()) {
            "join" -> {
                val name = json.get("data").asText()
                val user = User(uids.getAndIncrement(), name)
                users.put(session, user)
                // tell this user about other users
                emit(session, Message("users", users.values))
                // tell other users about this user
                broadcastToOthers(session, Message("join", user))
            }
            "say" -> {
                broadcast(Message("say", json.get("data").asText()))
            }
        }
        println("json msg ${message}")
    }

    @OnWebSocketClose
    fun disconnect(session: Session, code: Int, reason: String?) {
        // remove the user that d/c
        val user = users.remove(session)
        // notify all others of d/c user
        if (user != null) broadcast(Message("left", user))
        //todo stuff on disconnect... maybe sound master caution?
        println("> Socket D/C: ${reason}")
    }

    fun emit(session: Session, message: Message) {
        session.remote.sendString(jacksonObjectMapper().writeValueAsString(message))
    }
    fun broadcast(message: Message) {
        users.forEach(){emit(it.key, message)}
    }
    fun broadcastToOthers(session: Session, message: Message) {
        users.filter{it.key != session}.forEach() {
            emit(it.key, message)
        }
    }
}