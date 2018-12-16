package com.softwaremill.sttp.impl.zio

import com.softwaremill.sttp.MonadAsyncError
import scalaz.zio.{ExitResult, IO}

object ZIOMonadAsyncError extends MonadAsyncError[Lambda[A => IO[Throwable, A]]] {
  
  override def async[T](register: (Either[Throwable, T] => Unit) => Unit) =
    IO.async { cb: (ExitResult[Throwable, T] => Unit) =>
      register {
        case Left(t)  => cb(ExitResult.failed(ExitResult.Cause.unchecked(t)))
        case Right(t) => cb(ExitResult.succeeded(t))
      }
    }

  override def unit[T](t: T): IO[Nothing, T] = IO.point(t)

  override def map[T, T2](fa: IO[Throwable, T])(f: T => T2): IO[Throwable, T2] = fa.map(f)

  override def flatMap[T, T2](fa: IO[Throwable, T])(f: T => IO[Throwable, T2]): IO[Throwable, T2] = fa.flatMap(f)

  override def error[T](t: Throwable) = IO.fail(t)

  override protected def handleWrappedError[T](rt: IO[Throwable, T])(
      h: PartialFunction[Throwable, IO[Throwable, T]]): IO[Throwable, T] = rt.catchAll(h)
}
