import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object Common {

  /**
   * Callback types that are triggered upon Task completion.
   */
  type Callback = () => Unit
  type FailCallback = String => Unit

  def log(message: String) {
      println(message)
  }

  private val jsonMapper = new ObjectMapper with ScalaObjectMapper

  jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
  jsonMapper.registerModule(DefaultScalaModule)

  /**
   * Parse the SQS message to extract the jobRequestId
   *
   * @param messageString The SQS message body.
   *
   * @return Option[String]
   */
  def getJobRequestId(messageString: String): Option[String] = {
    try {
      val jsonBody = jsonMapper.readValue[Map[String, String]](messageString)

      jsonBody.get("jobRequestId")
    }
    catch {
      case _: JsonParseException =>
        log(s"Invalid message: $messageString")
        None
    }
  }

}

