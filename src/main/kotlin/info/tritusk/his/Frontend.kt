package info.tritusk.his

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.iyanuadelekan.kanary.app.KanaryApp
import com.iyanuadelekan.kanary.core.KanaryController
import com.iyanuadelekan.kanary.core.KanaryRouter
import com.iyanuadelekan.kanary.handlers.AppHandler
import com.iyanuadelekan.kanary.helpers.http.request.*
import com.iyanuadelekan.kanary.helpers.http.response.*
import com.iyanuadelekan.kanary.middleware.*
import com.iyanuadelekan.kanary.server.Server
import org.eclipse.jetty.server.Request
import redis.clients.jedis.Jedis
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class Frontend(private val dbClient : Jedis) {

    fun start(port: Int) {
        Server().apply {
            this.handler = AppHandler(KanaryApp().apply {
                this.mount(KanaryRouter().apply {
                    this on "his/" use KanaryController()
                    this.get("display/", ::display)
                    this.get("json/", ::sendJson)
                })
                this.use(simpleConsoleRequestLogger)
            })
        }.listen(port)
    }

    private fun display(baseReq: Request, req: HttpServletRequest, res: HttpServletResponse) {
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
        res withStatus 200 sendHtml """
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
                Site powered by <a href=https://github.com/xetorthio/jedis>Jedis</a>, <a href=https://jsoup.org/>Jsoup</a>, <a href=https://seunadelekan.github.io/Kanary>Kanary</a> and their dependencies
                <br/>
                Theme powered by <a href=https://github.com/mblode/marx>mblode/marx</a>
                </footer>
                </main>
                </body>
            """.trimIndent()
        baseReq.done()
    }

    private fun sendJson(baseReq: Request, req: HttpServletRequest, res: HttpServletResponse) {
        val timestamp = System.currentTimeMillis() / 600000
        if (this.dbClient.exists(timestamp.toString())) {
            val data = this.dbClient.hgetAll(timestamp.toString())
            res withStatus 200 sendJson ObjectMapper().valueToTree(data)
        } else {
            res withStatus 200 sendJson JsonNodeFactory.instance.objectNode()
        }
        baseReq.done()
    }

}