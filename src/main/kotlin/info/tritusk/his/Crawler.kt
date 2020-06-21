package info.tritusk.his

import org.jsoup.Jsoup
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import redis.clients.jedis.JedisPool

class Crawler(private val pool : JedisPool) : Runnable {

    companion object {
        val MARKER: Marker = MarkerFactory.getMarker("Crawler")

        val REMAP_KEY = mapOf(
                "公告" to "notice",
                "原创Mod" to "original",
                "转载Mod" to "repost",
                "指路贴" to "guide",
                "独立Mod" to "standalone",
                "附属Mod" to "addon",
                "索引帖" to "index",
                "索引贴" to "index"
        )
    }

    internal var running = true

    override fun run() {
        var lastTime = System.currentTimeMillis() / 600000
        while (running) {
            val currentTime = System.currentTimeMillis() / 600000
            if (currentTime - lastTime > 0) {
                try {
                    pool.resource.use {
                        if (it.hmset(currentTime.toString(), this.retrieve()) == "OK") {
                            lastTime = currentTime
                        } else {
                            LOGGER.warn(MARKER, "Failed to write database")
                        }
                    }
                } catch(e: Exception) {
                    LOGGER.error(MARKER, "Error occurred while retrieving latest data. ", e)
                }
            }
            Thread.sleep(10000)
        }
    }

    private fun retrieve(): Map<String, String> = Jsoup.connect("https://www.mcbbs.net/forum-mod-1.html")
            .userAgent("HISTORIA IN SPECULO")
            .get()
            .getElementsByClass("ttp bm cl")
            .select("a[href]")
            .filter { !it.children().isEmpty() }
            .associate { (REMAP_KEY[it.ownText()] ?: it.ownText()) to it.child(0).ownText() }
}