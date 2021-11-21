import java.sql.SQLException

import scala.collection.JavaConverters._

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

  /**
   * Thread pool for concurrent processing of messages.
   */
  private val threadPool = new ThreadPool(threadPoolSize)

  /**
   * Extract queue name from URL
   */
  private val queueName = queueURL.substring(queueURL.lastIndexOf('/') + 1)

  /**
   * SQS Client
   */
  private val sqsClient = AmazonSQSClientBuilder.standard()
    .withRegion(Regions.US_EAST_1)
    .build()

  /**
   * Set to true to initiate shutdown sequence.
   */
  private var shutdownFlag = false

  def shutdown() = { this.shutdownFlag = true }

  /**
   * Create callback to delete queue message upon success.
   *
   * @param receiptHandle The SQS message receipt handle
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
   * @param jobRequestId  The request ID of the message.
   * @param receiptHandle The SQS message receipt handle.
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
   * Main Consumer run loop performs the following steps.
   *   Consume messages
   *   Convert message to Task object
   *   Submit tasks to the ThreadPool
   *   Sleep based on the configured poll interval.
   */
  def run(): Unit = {
    while (!this.shutdownFlag) {
      val receiveMessageResult = sqsClient.receiveMessage(new ReceiveMessageRequest(queueURL)
        .withMaxNumberOfMessages(threadPoolSize))
      val messages = receiveMessageResult.getMessages
      val tasks = getTasks(messages.asScala.toList)

      threadPool.submitTasks(tasks, sqsConfig.requestTimeoutMinutes)

      Thread.sleep(sqsConfig.pollIntervalSeconds * 1000)
    }

    threadPool.shutdown()
  }

  /**
   * Convert the list of SQS Message to a list of Task
   *
   * @param messages List of SQS messages
   * @param result Accumulator for the result list of Tasks
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
   * @param message SQS Message
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
