import java.util.Properties

/**
 * A container for the SQS configuration parameters.
 *
 * @param props Configuration properties
 */
class SQSConfig(val props: Properties) {
  def getIntConfigValue(key: String, default: Int): Int = {
    val configValue = props.getProperty(key)

    if (configValue == null) {
      default
    }
    else {
      configValue.toInt
    }
  }

  /**
   * How long to wait in between polling for SQS messages
   */
  val pollIntervalSeconds = getIntConfigValue("sqs.poll.interval.seconds", 60)

  /**
   * Cutoff time for an individual queue message to be processed.
   */
  val requestTimeoutMinutes = getIntConfigValue("sqs.request.timeout.minutes", 240)
}
