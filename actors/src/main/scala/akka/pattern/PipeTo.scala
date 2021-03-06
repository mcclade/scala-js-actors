package akka.pattern

import language.implicitConversions

import scala.concurrent.{ Future, ExecutionContext }
import scala.util.{ Failure, Success }

import akka.actor._

trait PipeToSupport {

  final class PipeableFuture[T](val future: Future[T])(implicit executionContext: ExecutionContext) {
    def pipeTo(recipient: ActorRef)(implicit sender: ActorRef = Actor.noSender): Future[T] = {
      future onComplete {
        case Success(r) => recipient ! r
        case Failure(f) => recipient ! Status.Failure(f)
      }
      future
    }
    /*def pipeToSelection(recipient: ActorSelection)(implicit sender: ActorRef = Actor.noSender): Future[T] = {
      future onComplete {
        case Success(r) => recipient ! r
        case Failure(f) => recipient ! Status.Failure(f)
      }
      future
    }*/
    def to(recipient: ActorRef): PipeableFuture[T] = to(recipient, Actor.noSender)
    def to(recipient: ActorRef, sender: ActorRef): PipeableFuture[T] = {
      pipeTo(recipient)(sender)
      this
    }
    /*def to(recipient: ActorSelection): PipeableFuture[T] = to(recipient, Actor.noSender)
    def to(recipient: ActorSelection, sender: ActorRef): PipeableFuture[T] = {
      pipeToSelection(recipient)(sender)
      this
    }*/
  }

  /**
   * Import this implicit conversion to gain the `pipeTo` method on [[scala.concurrent.Future]]:
   *
   * {{{
   * import akka.pattern.pipe
   *
   * Future { doExpensiveCalc() } pipeTo nextActor
   *
   * or
   *
   * pipe(someFuture) to nextActor
   *
   * }}}
   *
   * The successful result of the future is sent as a message to the recipient, or
   * the failure is sent in a [[akka.actor.Status.Failure]] to the recipient.
   */
  implicit def pipe[T](future: Future[T])(implicit executionContext: ExecutionContext): PipeableFuture[T] = new PipeableFuture(future)
}

object PipeTo extends PipeToSupport
