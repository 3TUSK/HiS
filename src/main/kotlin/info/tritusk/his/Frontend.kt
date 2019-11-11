package info.tritusk.his

import io.javalin.Javalin
import redis.clients.jedis.Jedis
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Frontend(private val dbClient : Jedis) {

    fun start(port: Int) {
        Javalin.create { config ->
            config.showJavalinBanner = false
            config.addStaticFiles("/static")
        }.start(port).apply {
            this.get("json") { ctx ->
                val timestamp = try {
                    ctx.queryParam("time")?.toLong() ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                } / 600000
                ctx.status(200).json(this@Frontend.dbClient.hgetAll(timestamp.toString()))
            }
        }
    }

}