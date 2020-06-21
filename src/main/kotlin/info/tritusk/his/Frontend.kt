package info.tritusk.his

import io.javalin.Javalin
import redis.clients.jedis.JedisPool

class Frontend(private val pool : JedisPool) {

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
                    this@Frontend.pool.resource.use {
                        ctx.json(it.hgetAll(timestamp.toString()))
                        ctx.status(200)
                    }
                } catch (e: Exception) {
                    LOGGER.error("An error occurred while querying database. Details: ", e)
                }
            }
        }
    }

}