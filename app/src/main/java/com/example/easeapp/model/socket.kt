//import io.socket.client.IO
//import io.socket.client.Socket
//import kotlinx.coroutines.Dispatchers.IO
//import org.json.JSONObject
//import java.net.Socket
//import java.net.URISyntaxException
//
//class ChatSocketManager {
//
//    private lateinit var socket: Socket
//
//    fun connect(userId: String, meetingId: String, role: String) {
//        try {
//            socket = IO.socket("http://your-server-url") // החלף לכתובת שלך
//            socket.connect()
//
//            socket.on(Socket.EVENT_CONNECT) {
//                println("Connected to server")
//
//                // שליחת בקשת הצטרפות לחדר
//                val joinData = JSONObject().apply {
//                    put("userId", userId)
//                    put("meetingId", meetingId)
//                    put("role", role)
//                }
//                socket.emit("joinRoom", joinData)
//            }
//
//            // האזנה להודעות נכנסות
//            socket.on("newMessage") { args ->
//                val data = args[0] as JSONObject
//                val from = data.getString("from")
//                val message = data.getString("message")
//                val timestamp = data.getString("timestamp")
//
//                // כאן אתה יכול לעדכן את ה־UI
//                println("[$timestamp] $from: $message")
//            }
//
//            // האזנה לאירועים נוספים אם רוצים
//            socket.on("consultationStarted") {
//                println("Consultation started")
//            }
//
//            socket.on("consultationEnded") { args ->
//                val data = args[0] as JSONObject
//                val summary = data.getString("summary")
//                println("Consultation ended. Summary: $summary")
//            }
//
//        } catch (e: URISyntaxException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendMessage(meetingId: String, from: String, to: String, message: String) {
//        val msgData = JSONObject().apply {
//            put("meetingId", meetingId)
//            put("from", from)
//            put("to", to)
//            put("message", message)
//            put("timestamp", System.currentTimeMillis())
//        }
//        socket.emit("sendMessage", msgData)
//    }
//
//    fun disconnect() {
//        socket.disconnect()
//        socket.off()
//    }
//}
