package net.asqueados.wvscrap

import io.circe.syntax._
import java.io._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import io.circe.Encoder


object WVExporter {
    private val BasePath = "out/"
    private val DateFormat = DateTimeFormatter.ofPattern("yyMMddhhmmss")

    def export[T <: Exportable](entity: T)(implicit encoder: Encoder[T]): Unit =
        writeToFile(entity.title, entity.asJson.toString)


    private def writeToFile(suffix: String, content: String) = {

        val file = new File(path(suffix))
        val bw = new BufferedWriter(new FileWriter(file))
        bw.write(content)
        bw.close()
    }

    private def path(suffix: String) =
        BasePath + LocalDateTime.now.format(DateFormat) + "_" + sanitize(suffix) + ".json"

    private def sanitize(filename: String) =
        filename.replaceAll("""[*\[\]()/!"'\?]""", "")
            .trim
            .replace(" ", "-")


}
