import java.util.Properties

/**
 * A container for the SQS configuration parameters.
 *
 * @param props
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

  val pollIntervalSeconds = getIntConfigValue("sqs.poll.interval.seconds", 60)

  val requestTimeoutMinutes = getIntConfigValue("sqs.request.timeout.minutes", 240)
}
