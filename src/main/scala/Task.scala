import java.util.concurrent.Callable
import java.io.{PrintWriter, StringWriter}

import Common.{Callback, FailCallback}

/**
 * Task for executing in ThreadPool.  Execute the specified jobRequestID and then
 * invoke the success or fail callback.
 *
 * @param jobRequestId  The Id of the message request.
 * @param successCallBack Called upon success.
 * @param failCallback  Called upon failure.
 */
class Task(val jobRequestId: String, successCallBack: Callback,
           failCallback: FailCallback)
  extends Callable[Unit] {

  override def call(): Unit = {
    try {
      // Insert code to process request for: jobRequestId
      
      successCallBack()
    }
    catch {
      case ex: Exception =>
        val errorMessage = ex.getMessage

        failCallback(errorMessage)
    }
  }
}
