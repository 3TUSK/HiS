package info.tritusk.his

import io.javalin.Javalin
import redis.clients.jedis.Jedis
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class Frontend(private val dbClient : Jedis) {

    fun start(port: Int) {
        Javalin.create().start(port).apply {
            this.get("his/") { ctx ->
                ctx.status(200).html(display())
            }
            this.get("his/json") { ctx ->
                val timestamp = try {
                    ctx.queryParam("time")?.toLong() ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                } / 600000
                ctx.status(200).json(this@Frontend.dbClient.hgetAll(timestamp.toString()))
            }
        }
    }

    private fun display(): String {
        val timestamp = System.currentTimeMillis() / 600000
        val data = try {
            this.dbClient.hgetAll(timestamp.toString())
        } catch (e: Exception) {
            emptyMap<String, String>()
        }
        val countRepost = data["repost"] ?: "unknown"
        val countOriginal = data["original"] ?: "unknown"
        val ratio = try {
            countRepost.toInt().toDouble() / countOriginal.toInt()
        } catch (e: Exception) {
            -1.0
        }
        return """
                <!DOCTYPE html>
                <head>
                <meta charset="utf-8"/>
                <title>HiS: The ratio counter</title>
                <link rel="stylesheet" href="https://unpkg.com/marx-css/css/marx.min.css"/>
                </head>
                <body>
                <main>
                <center><h1>Historia in Speculō</h1></center>
                <hr/>
                <section>
                At this moment, there are $countRepost mod repost threads and $countOriginal original mod release threads on mcbbs.net.
                <br/>
                The ratio of repost thread to original threads is $ratio.
                <br/>
                Latest data are harvested at ${Instant.ofEpochMilli(timestamp * 600000).atZone(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME)}
                </section>
                <footer>
                Site powered by <a href=https://github.com/FasterXML/jackson-databind>Jackson-databind</a>, <a href=https://javalin.io/>Javalin</a>, <a href=https://github.com/xetorthio/jedis>Jedis</a>, <a href=https://jsoup.org/>Jsoup</a> and their dependencies
                <br/>
                Theme powered by <a href=https://github.com/mblode/marx>mblode/marx</a>
                </footer>
                </main>
                </body>
            """.trimIndent()
    }

}