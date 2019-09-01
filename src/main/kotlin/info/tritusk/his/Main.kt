package info.tritusk.his

import joptsimple.OptionParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.MarkerFactory
import redis.clients.jedis.Jedis

val LOGGER: Logger = LoggerFactory.getLogger("HiS")
val MARKER: Marker = MarkerFactory.getMarker("Main")

fun main(args: Array<String>) {

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

    val dbClient = Jedis(options.valueOf(dbAddress), options.valueOf(dbPort))
    dbClient.ping()
    if (!dbClient.isConnected) {
        LOGGER.info(MARKER, "HiS cannot connect to Redis database on ${options.valueOf(dbAddress)}:${options.valueOf(dbPort)}")
        LOGGER.info(MARKER, "HiS is shutting down")
        try {
            dbClient.quit()
        } catch (ignored: Exception) {}
        return
    }

    LOGGER.info(MARKER, "Starting crawler...")

    val crawler = Crawler(dbClient)
    val crawlerRunner = Thread(crawler).apply { this.start() }
    Runtime.getRuntime().addShutdownHook(Thread {
        crawler.running = false
        LOGGER.info(MARKER, "Shutting down crawler...")
        crawlerRunner.join()
    })

    LOGGER.info(MARKER, "Starting web server...")

    Frontend(dbClient).start(options.valueOf(serverPort))
}