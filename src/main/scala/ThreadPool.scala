import java.util.concurrent.{Executors, ThreadPoolExecutor, TimeUnit}
import scala.collection.JavaConverters._

/**
 * An object encapsulating a pool of threads for executing Tasks.
 */
class ThreadPool(threadPoolSize: Int) {

  val executor: ThreadPoolExecutor =
    Executors.newFixedThreadPool(threadPoolSize).asInstanceOf[ThreadPoolExecutor]

  /**
   * Submit a list of tasks for execution and wait for completion.
   *
   * @param tasks List of tasks to submit to thread pool
   */
  def submitTasks(tasks: List[Task], requestTimeoutMinutes: Long): Unit = {
    executor.invokeAll(tasks.asJava, requestTimeoutMinutes, TimeUnit.MINUTES)
  }

  /**
   * Shutdown the ExecuterService gracefully
   *
   * @param timeout Timeout in minutes.
   */
  def shutdown(): Unit = {
    executor.shutdown()
  }
}
