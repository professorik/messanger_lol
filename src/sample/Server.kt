package sample.API

import org.json.simple.JSONArray
import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser
import java.lang.Exception
import java.lang.RuntimeException
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.thread
import kotlin.math.floor

fun String.base64Decode(): ByteArray = Base64.getDecoder().decode(this)

data class MutablePair<T, E>(var first: T, var second: E)

abstract class AppServerConnection: EventEmitter<JSONObject>() {
    abstract fun send(data: JSONObject)
    abstract fun close()
}

class AppSocketServerConnection(val socket: Socket): AppServerConnection() {
    val thread: Thread

    init {
        thread = thread(true) {
            while (true) {
                val lengthArray = socket.readBlocking(4)
                var length = 0
                for (i in 0..3) length += lengthArray[i] shl (i*8)
                val string = String(socket.readBlocking(length).map { it.toByte() }.toByteArray(), StandardCharsets.UTF_8)
                try { emit(JSONParser().parse(string) as JSONObject) } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    override fun send(data: JSONObject) {
        val byteArray = data.toJSONString().toByteArray()
        var byteArrayLength = byteArray.size
        for (i in 0..3) socket.getOutputStream().write((byteArrayLength shr (i*8)) and 0xFF)
        socket.getOutputStream().write(byteArray)
    }

    override fun close() {
        thread.stop()
        socket.close()
    }
}

data class Message(val fromId: Int, val toId: Int, val value: String, val timestamp: Int)
data class User(val id: Int, val username: String, val name: String?, val password: String, val profilePic: ByteArray?)
data class Token(val id: Int, val userId: Int, val value: String)

class DatabaseConnection {
    val connection: Connection
    init {
        connection = DriverManager.getConnection("jdbc:sqlite:database.db")
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS users (\n" +
                        "id integer PRIMARY KEY,\n" +
                        "username string NOT NULL,\n" +
                        "name string,\n" +
                        "password string,\n" +
                        "profile_pic string\n" +
                        ");"
        )
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS tokens (\n" +
                        "id integer PRIMARY KEY,\n" +
                        "user_id integer NOT NULL,\n" +
                        "value string NOT NULL\n" +
                        ");"
        )
        connection.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS messages (\n" +
                        "id integer PRIMARY KEY,\n" +
                        "from_id integer NOT NULL,\n" +
                        "to_id integer NOT NULL,\n" +
                        "type integer NOT NULL,\n" +
                        "value string NOT NULL,\n" +
                        "sent integer NOT NULL\n" +
                        ");"
        )
    }
    fun getToken(value: String): Token? {
        val statement = connection
                .prepareStatement("SELECT * FROM tokens WHERE value = ?")
        statement.setString(1, value)
        val result = statement.executeQuery()
        return if (result.next())
            Token(result.getInt("id"), result.getInt("user_id"),
                    result.getString("value"))
        else null
    }
    fun getUser(username: String): User? {
        val statement = connection
                .prepareStatement("SELECT * FROM users WHERE username = ?")
        statement.setString(1, username)
        val result = statement.executeQuery()
        return if (result.next())
            User(result.getInt("id"), result.getString("username"), result.getString("name"),
                    result.getString("password"), result.getString("profile_pic")?.base64Decode())
        else null
    }
    private fun encodePassword(password: String) = MessageDigest.getInstance("SHA1").digest(password.toByteArray())
            .joinToString("") { "%02x".format(it) }
    fun getUser(username: String, password: String): User? {
        val encoded = encodePassword(password)
        val statement = connection
                .prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")
        statement.setString(1, username)
        statement.setString(2, encoded)
        val result = statement.executeQuery()
        return if (result.next())
            User(result.getInt("id"), result.getString("username"), result.getString("name"),
                    result.getString("password"), result.getString("profile_pic")?.base64Decode())
        else null
    }
    fun getUser(id: Int): User? {
        val statement = connection
                .prepareStatement("SELECT * FROM users WHERE id = ?")
        statement.setInt(1, id)
        val result = statement.executeQuery()
        return if (result.next())
            User(result.getInt("id"), result.getString("username"), result.getString("name"),
                    result.getString("password"), result.getString("profile_pic")?.base64Decode())
        else null
    }
    fun createToken(userId: Int): String {
        var token = ""
        for (i in 0..19) {
            val random = floor(Math.random() * (10 + 26 * 2)).toInt()
            token += when (random) {
                in 0..9 -> '0' + random
                in 10..35 -> 'a' + random - 10
                in 36..61 -> 'A' + random - 36
                else -> throw RuntimeException()
            }
        }
        val statement = connection
                .prepareStatement("INSERT INTO tokens(user_id, value) VALUES (?, ?)")
        statement.setInt(1, userId)
        statement.setString(2, token)
        statement.execute()
        return token
    }
    fun createUser(username: String, name: String? = null, password: String) {
        val encoded = encodePassword(password)
        val statement = connection
                .prepareStatement("INSERT INTO users(username, name, password) VALUES (?, ?, ?)")
        statement.setString(1, username)
        statement.setString(2, name)
        statement.setString(3, encoded)
        statement.execute()
    }
    fun createMessage(fromId: Int, toId: Int, value: String) {
        val statement = connection
                .prepareStatement("INSERT INTO messages(from_id, to_id, type, value, sent) VALUES (?, ?, 0, ?, ?)")
        statement.setInt(1, fromId)
        statement.setInt(2, toId)
        statement.setString(3, value)
        statement.setInt(4, (Date().time / 1000).toInt())
        statement.execute()
    }
    fun deleteToken(token: String) {
        val statement = connection
                .prepareStatement("DELETE FROM tokens WHERE value = ?")
        statement.setString(1, token)
        statement.execute()
    }
    private fun messageResultToMessageList(queryResult: ResultSet): List<Message> {
        val result = ArrayList<Message>()
        while (queryResult.next()) {
            result.add(Message(
                    queryResult.getInt("from_id"), queryResult.getInt("to_id"),
                    queryResult.getString("value"), queryResult.getInt("sent")
            ))
        }
        return result
    }
    fun getLastMessages(fromId: Int, toId: Int, count: Int, offset: Int): List<Message> {
        val statement = connection
                .prepareStatement("SELECT * FROM messages WHERE (from_id = ? AND to_id = ?) OR (from_id = ? AND to_id = ?) ORDER BY sent DESC LIMIT ? OFFSET ?")
        statement.setInt(1, fromId)
        statement.setInt(2, toId)
        statement.setInt(3, toId)
        statement.setInt(4, fromId)
        statement.setInt(5, count)
        statement.setInt(6, offset)
        val queryResult = statement.executeQuery()
        return messageResultToMessageList(queryResult)
    }
    fun getLastMessages(fromId: Int, count: Int, offset: Int): List<Message> {
        val statement = connection
                .prepareStatement("SELECT * FROM messages WHERE from_id = ? OR to_id = ? ORDER BY sent DESC LIMIT ? OFFSET ?")
        statement.setInt(1, fromId)
        statement.setInt(2, fromId)
        statement.setInt(3, count)
        statement.setInt(4, offset)
        val queryResult = statement.executeQuery()
        return messageResultToMessageList(queryResult)
    }
    fun setProfilePic(id: Int, pic: ByteArray?) {
        val statement = connection
                .prepareStatement("UPDATE users SET profile_pic = ? WHERE id = ?")
        statement.setString(1, if (pic != null) Base64.getEncoder().encodeToString(pic) else null)
        statement.setInt(2, id)
        statement.execute()
    }
    fun getChats(with: Int): List<String> {
        val statement = connection
                .prepareStatement("SELECT DISTINCT username FROM users LEFT JOIN messages ON ((messages.from_id = users.id AND messages.to_id = ?) OR (messages.to_id = users.id AND messages.from_id = ?)) WHERE messages.id IS NOT NULL AND users.id != ?")
        statement.setInt(1, with)
        statement.setInt(2, with)
        statement.setInt(3, with)
        val resultSet = statement.executeQuery()
        val result = ArrayList<String>()
        while (resultSet.next()) result.add(resultSet.getString("username"))
        return result
    }
}

class AppServer(val socketPort: Int): EventEmitter<AppServerConnection>() {
    val socketServer = ServerSocket(socketPort)
    val databaseConnection = DatabaseConnection()

    init {
        thread(true) {
            while (true) {
                val socket = AppSocketServerConnection(socketServer.accept())
                emit(socket)
            }
        }

        val sessions = ArrayList<MutablePair<AppServerConnection, String?>>()

        addListener kar@ { socket ->
            val pair = MutablePair<AppServerConnection, String?>(socket, null)
            sessions.add(pair)
            socket.addListener { query ->
                val response = handleQuery(sessions, query, pair)
                if (response["token"] as? String != null) {
                    pair.second = response["token"]!! as String
                }
                socket.send(response)
            }
        }
    }

    private fun handleQuery(sessions: ArrayList<MutablePair<AppServerConnection, String?>>,
                            query: JSONObject, currentSession: MutablePair<AppServerConnection, String?>): JSONObject {
        val queryId = query["_request_id"] as? Long

        fun error(message: String): JSONObject {
            val response = JSONObject()
            response["success"] = false
            response["error"] = message
            if (queryId != null) response["_request_id"] = queryId
            return response
        }

        fun successResponse(): JSONObject {
            val response = JSONObject()
            response["success"] = true
            if (queryId != null) response["_request_id"] = queryId
            return response
        }

        fun getToken() = query["token"] as? String ?: currentSession.second

        fun verifyToken(): Boolean =
                databaseConnection.getToken(
                        getToken() ?: ""
                ) != null

        fun getUserFromToken(): User? {
            val token = databaseConnection.getToken( getToken() ?: return null ) ?: return null
            return databaseConnection.getUser(token.userId)
        }

        val queryType = query["type"] as? String ?: return error("Query type not specified")

        return when (queryType) {
            "login" -> {
                val username = (query["username"] as? String) ?: return error("Username not specified")
                val password = (query["password"] as? String) ?: return error("Password not specified")
                val user = databaseConnection.getUser(username, password) ?: return error("User not found")
                val token = databaseConnection.createToken(user.id)
                val response = successResponse()
                response["token"] = token
                response
            }
            "register" -> {
                val username = (query["username"] as? String) ?: return error("Username not specified")
                val password = (query["password"] as? String) ?: return error("Password not specified")
                val name = query["name"] as? String
                if (username.length < 4 || !username.all { it in '0'..'9' || it in 'a'..'z' || it in 'A'..'Z' || it == '$' || it == '_' || it == '-' })
                    return error("Username is too short or contains invalid characters")
                if (password.length < 6) return error("Password is too short")
                if (databaseConnection.getUser(username) != null) return error("This user already exists")
                databaseConnection.createUser(username, name, password)
                successResponse()
            }
            "findUser" -> {
                if (!verifyToken()) return error("Bad token")
                val username = (query["username"] as? String) ?: return error("Username not specified")
                val user = databaseConnection.getUser(username) ?: return error("No such user found")
                val response = successResponse()
                response["username"] = user.username
                response["name"] = user.name
                response["profilePicture"] = String(Base64.getEncoder().encode(user.profilePic))
                response
            }
            "setProfilePicture" -> {
                val user = getUserFromToken() ?: return error("Bad token")
                val pic = ((query["base64"] as? String) ?: return error("Username not specified")).base64Decode()
                if (pic.size > 1024 * 1024 * 10) return error("The picture is too big")
                databaseConnection.setProfilePic(user.id, pic)
                successResponse()
            }
            "deleteProfilePicture" -> {
                val user = getUserFromToken() ?: return error("Bad token")
                databaseConnection.setProfilePic(user.id, null)
                successResponse()
            }
            "sendMessage" -> {
                val user = getUserFromToken() ?: return error("Bad token")
                val username = (query["username"] as? String) ?: return error("Username not specified")
                val value = (query["value"] as? String) ?: return error("Value not specified")
                val receiver = databaseConnection.getUser(username) ?: return error("No such user exists")
                databaseConnection.createMessage(user.id, receiver.id, value)
                sessions.find {
                    val token = databaseConnection.getToken(it.second ?: return@find false) ?: return@find false
                    val itUser = databaseConnection.getUser(token.userId) ?: return@find false
                    if (itUser.id == receiver.id) {
                        val notification = JSONObject()
                        notification["type"] = "message"
                        notification["from"] = user.username
                        notification["value"] = value
                        notification["timestamp"] = (Date().time / 1000).toInt()
                        it.first.send(notification)
                        true
                    } else false
                }
                successResponse()
            }
            "currentToken" -> {
                if (currentSession.second == null) return error("Current token is not set for this session")
                else {
                    val response = successResponse()
                    response["currentToken"] = currentSession.second
                    response
                }
            }
            "setCurrentToken" -> {
                if (!verifyToken() || (query["token"] as? String) == null) return error("Bad token")
                currentSession.second = query["token"] as String
                successResponse()
            }
            "deleteCurrentToken" -> {
                currentSession.second = null
                successResponse()
            }
            "logout" -> {
                if (!verifyToken()) return error("Bad token")
                databaseConnection.deleteToken(getToken() ?: return error("wtf"))
                successResponse()
            }
            "getLastMessagesFrom" -> {
                val user = getUserFromToken() ?: return error("Bad token")
                val username = (query["username"] as? String) ?: return error("Username not specified")
                val offset = ((query["offset"] as? Long) ?: 0L).toInt()
                val receiver = databaseConnection.getUser(username) ?: return error("No such user exists")
                val messages = databaseConnection.getLastMessages(user.id, receiver.id, 100, offset)
                val messagesResponse = JSONArray()
                messages.forEach {
                    val messageObject = JSONObject()
                    messageObject["value"] = it.value
                    messageObject["timestamp"] = it.timestamp
                    messageObject["from"] = if (it.fromId == user.id) user.username else receiver.username
                    messageObject["to"] = if (it.toId == user.id) user.username else receiver.username
                    messagesResponse.add(messageObject)
                }
                val response = successResponse()
                response["messages"] = messagesResponse
                response
            }
            "getLastMessages" -> {
                val user = getUserFromToken() ?: return error("Bad token")
                val offset = ((query["offset"] as? Long) ?: 0L).toInt()
                val messages = databaseConnection.getLastMessages(user.id, 100, offset)
                val messagesResponse = JSONArray()
                messages.forEach {
                    val messageObject = JSONObject()
                    messageObject["value"] = it.value
                    messageObject["timestamp"] = it.timestamp
                    val other = databaseConnection.getUser(if (it.fromId == user.id) it.toId else it.fromId)
                            ?: return error("wtf")
                    messageObject["from"] = if (it.fromId == user.id) user.username else other.username
                    messageObject["to"] = if (it.toId == user.id) user.username else other.username
                    messagesResponse.add(messageObject)
                }
                val response = successResponse()
                response["messages"] = messagesResponse
                response
            }
            "getChats" -> {
                val user = getUserFromToken() ?: return error("Bad token")
                val chats = JSONArray()
                databaseConnection.getChats(user.id).forEach { chats.add(it) }
                val response = successResponse()
                response["chats"] = chats
                response
            }
            else -> error("Unknown query type")
        }
    }
}
