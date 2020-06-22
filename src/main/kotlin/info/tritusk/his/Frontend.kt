package info.tritusk.his

import io.javalin.Javalin
import redis.clients.jedis.JedisPool

class Frontend(private val pool : JedisPool) {

    private val errorPayload = mapOf("error" to "Failed to query data")

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
                try {
                    this@Frontend.pool.resource.use {
                        ctx.status(200).json(it.hgetAll(timestamp.toString()))
                    }
                } catch (e: Exception) {
                    LOGGER.error("An error occurred while querying database. Details: ", e)
                    ctx.status(500).json(errorPayload)
                }
            }
        }
    }

}