package org.thp.cortex.services

import java.nio.file._

import scala.concurrent.{ ExecutionContext, Future }

import play.api.{ Configuration, Logger }

import com.spotify.docker.client.messages.HostConfig.Bind
import com.spotify.docker.client.messages.{ ContainerConfig, HostConfig }
import com.spotify.docker.client.{ DefaultDockerClient, DockerClient }
import javax.inject.{ Inject, Singleton }
import org.thp.cortex.models._

@Singleton
class DockerJobRunnerSrv(client: DockerClient) {

  def this() = this(DefaultDockerClient.fromEnv().build())

  @Inject()
  def this(config: Configuration) = this(new DefaultDockerClient.Builder()
    .apiVersion(config.getOptional[String]("docker.version").orNull)
    .connectionPoolSize(config.getOptional[Int]("docker.connectionPoolSize").getOrElse(100))
    .connectTimeoutMillis(config.getOptional[Long]("docker.connectTimeoutMillis").getOrElse(5000))
    //.dockerCertificates()
    .readTimeoutMillis(config.getOptional[Long]("docker.readTimeoutMillis").getOrElse(30000))
    //.registryAuthSupplier()
    .uri(config.getOptional[String]("docker.uri").getOrElse("unix:///var/run/docker.sock"))
    .useProxy(config.getOptional[Boolean]("docker.useProxy").getOrElse(false))
    .build())

  lazy val logger = Logger(getClass)

  def run(jobDirectory: Path, dockerImage: String, job: Job)(implicit ec: ExecutionContext): Future[Unit] = {
    client.pull(dockerImage)
    //    ContainerConfig.builder().addVolume()
    val hostConfig = HostConfig.builder()
      .appendBinds(Bind.from(jobDirectory.toAbsolutePath.toString)
        .to("/job")
        .readOnly(false)
        .build())
      .build()
    val containerCreation = client.createContainer(ContainerConfig.builder()
      .hostConfig(hostConfig)
      .image(dockerImage)
      .cmd("/job")
      .build())
    //          Option(containerCreation.warnings()).flatMap(_.asScala).foreach(logger.warn)
    client.startContainer(containerCreation.id())

    Future {
      client.waitContainer(containerCreation.id())
      ()
    }
  }

}
