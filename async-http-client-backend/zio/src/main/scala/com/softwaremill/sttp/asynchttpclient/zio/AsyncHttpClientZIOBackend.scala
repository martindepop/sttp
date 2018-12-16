package com.softwaremill.sttp.asynchttpclient.zio

import java.nio.ByteBuffer

import com.softwaremill.sttp.asynchttpclient.AsyncHttpClientBackend
import com.softwaremill.sttp.impl.zio.ZIOMonadAsyncError
import com.softwaremill.sttp.{FollowRedirectsBackend, SttpBackend, SttpBackendOptions}
import io.netty.buffer.ByteBuf
import org.asynchttpclient.{AsyncHttpClient, AsyncHttpClientConfig, DefaultAsyncHttpClient}
import org.reactivestreams.Publisher

import scalaz.zio.IO

class AsyncHttpClientZIOBackend private (asyncHttpClient: AsyncHttpClient, closeClient: Boolean)
    extends AsyncHttpClientBackend[Lambda[A => IO[Throwable, A]], Nothing](asyncHttpClient,
                                                                           ZIOMonadAsyncError,
                                                                           closeClient) {

  override protected def streamBodyToPublisher(s: Nothing): Publisher[ByteBuf] =
    s // nothing is everything

  override protected def publisherToStreamBody(p: Publisher[ByteBuffer]): Nothing =
    throw new IllegalStateException("This backend does not support streaming")

  override protected def publisherToBytes(p: Publisher[ByteBuffer]): IO[Throwable, Array[Byte]] =
    throw new IllegalStateException("This backend does not support streaming")
}

object AsyncHttpClientZIOBackend {
  private def apply(asyncHttpClient: AsyncHttpClient,
                    closeClient: Boolean): SttpBackend[Lambda[A => IO[Throwable, A]], Nothing] =
    new FollowRedirectsBackend[Lambda[A => IO[Throwable, A]], Nothing](
      new AsyncHttpClientZIOBackend(asyncHttpClient, closeClient))

  def apply(
      options: SttpBackendOptions = SttpBackendOptions.Default): SttpBackend[Lambda[A => IO[Throwable, A]], Nothing] =
    AsyncHttpClientZIOBackend(AsyncHttpClientBackend.defaultClient(options), closeClient = true)

  def usingConfig(cfg: AsyncHttpClientConfig): SttpBackend[Lambda[A => IO[Throwable, A]], Nothing] =
    AsyncHttpClientZIOBackend(new DefaultAsyncHttpClient(cfg), closeClient = true)

  def usingClient(client: AsyncHttpClient): SttpBackend[Lambda[A => IO[Throwable, A]], Nothing] =
    AsyncHttpClientZIOBackend(client, closeClient = false)
}
