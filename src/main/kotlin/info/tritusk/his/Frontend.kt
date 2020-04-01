package info.tritusk.his

import io.javalin.Javalin
import redis.clients.jedis.Jedis

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
                ctx.status(500).json(mapOf("error" to "Failed to query data"))
                try {
                    synchronized(this@Frontend.dbClient) {
                        // TODO: This is definitely wrong, need to look deeper
                        ctx.json(this@Frontend.dbClient.hgetAll(timestamp.toString()))
                    }
                    ctx.status(200)
                } catch (e: Exception) {
                    System.err.println("An error occurred while querying database. Details: ")
                    System.err.println(e.localizedMessage)
                }
            }
        }
    }

}