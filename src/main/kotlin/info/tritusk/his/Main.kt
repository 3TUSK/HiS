package info.tritusk.his

import joptsimple.OptionParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import redis.clients.jedis.JedisPool
import redis.clients.jedis.JedisPoolConfig

val LOGGER: Logger = LoggerFactory.getLogger("HiS")
val MARKER: Marker = MarkerFactory.getMarker("Main")

fun main(vararg args: String) {

    val parser = OptionParser().also {
        it.allowsUnrecognizedOptions()
        it.accepts("help", "Print this").forHelp()
    }
    val dbAddress = parser.accepts("db-address").withRequiredArg().defaultsTo("localhost")
    val dbPort = parser.accepts("db-port").withRequiredArg().ofType(Int::class.java).defaultsTo(7379)
    val serverPort = parser.accepts("server-port").withRequiredArg().ofType(Int::class.java).defaultsTo(8080)

    val options = parser.parse(*args)

    if (options.has("help")) {
        parser.printHelpOn(System.out)
        return
    }

    val pool = JedisPool(JedisPoolConfig(), options.valueOf(dbAddress), options.valueOf(dbPort))

    LOGGER.info(MARKER, "Starting crawler...")

    val crawler = Crawler(pool)
    val crawlerRunner = Thread(crawler).apply { this.start() }
    Runtime.getRuntime().addShutdownHook(Thread {
        crawler.running = false
        LOGGER.info(MARKER, "Shutting down crawler...")
        crawlerRunner.join()
        pool.close()
    })

    LOGGER.info(MARKER, "Starting web server...")

    Frontend(pool).start(options.valueOf(serverPort))
}