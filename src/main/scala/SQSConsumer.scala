import java.sql.SQLException
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.{GetQueueAttributesRequest, ReceiveMessageRequest}

import scala.collection.JavaConversions._
import Common.log
import Common.{Callback, FailCallback}
import com.amazonaws.regions.Regions
import com.amazonaws.services.sqs.AmazonSQSClientBuilder


/**
 * Consume messages periodically based on the configured poll interval.
 *
 * @param sqsConfig
 * @param queueURL
 * @param threadPoolSize
 * @param dbConfig
 */
class SQSConsumer(sqsConfig: SQSConfig, queueURL: String, threadPoolSize: Int) extends Runnable {
  private val threadPool = new ThreadPool(threadPoolSize)
  private val queueName = queueURL.substring(queueURL.lastIndexOf('/') + 1)
  private val sqsClient = AmazonSQSClientBuilder.standard()
    .withRegion(Regions.US_EAST_1)
    .build()

  private var shutdownFlag = false

  def shutdown() = { this.shutdownFlag = true }

  /**
   * Create callback to delete queue message upon success.
   *
   * @param receiptHandle
   *
   * @return Option type for callback function
   */
  def createSuccessCallback(jobRequestId: String, receiptHandle: String): Callback = {
    () => {
      sqsClient.deleteMessage(queueURL, receiptHandle)
      ()
    }
  }

  /**
   * Create fail callback 
   *
   * @param jobRequestId
   * @param receiptHandle
   *
   * @return Option type for callback function
   */
  def createFailCallback(jobRequestId: String, receiptHandle: String): FailCallback = {
    (message: String) => {
      sqsClient.deleteMessage(queueURL, receiptHandle)

      // Insert any other fail logic
      ()
    }
  }

  /**
   * Main Consumer run loop
   */
  def run(): Unit = {
    while (!this.shutdownFlag) {
      val receiveMessageResult = sqsClient.receiveMessage(new ReceiveMessageRequest(queueURL)
        .withMaxNumberOfMessages(threadPoolSize))
      val messages = receiveMessageResult.getMessages

      import scala.collection.JavaConverters._
      val tasks = getTasks(messages.asScala.toList)

      threadPool.submitTasks(tasks, sqsConfig.requestTimeoutMinutes)

      log(s"SQSConsumer for queue: $queueName sleeping for %d seconds".
        format(sqsConfig.pollIntervalSeconds))
      Thread.sleep(sqsConfig.pollIntervalSeconds * 1000)
    }

    threadPool.shutdown()
    log((s"SQSConsumer queue: $queueName shutting down..."))
  }

  /**
   * Convert the list of SQS Message to a list of Task
   *
   * @param messages
   * @param result
   *
   * @return Task List
   */
  def getTasks(messages: List[Message], result: List[Task] = Nil): List[Task] = {
    messages match {
      case message::tail =>
        val taskOption: Option[Task] = getTask(message)

        taskOption match {
          case Some(task) => task::getTasks(tail, result)
          case None => getTasks(tail, result)
        }
      case Nil => result
    }
  }

  /**
   * Convert an individual SQS Message to a Task
   *
   * @param message
   *
   * @return Some(task) if a jobRequestId was parsed.
   */
  def getTask(message: Message): Option[Task] = {
      val jobRequestIdOption = Common.getJobRequestId(message.getBody)
      val receiptHandle = message.getReceiptHandle

      jobRequestIdOption match {
        case Some(jobRequestId: String) =>
          Some(new Task(jobRequestId, createSuccessCallback(jobRequestId, receiptHandle),
            createFailCallback(jobRequestId, receiptHandle)))
        case _ =>
          sqsClient.deleteMessage(queueURL, receiptHandle)
          None
      }
    }
}
