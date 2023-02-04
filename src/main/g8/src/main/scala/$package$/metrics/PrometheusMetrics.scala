package $package$.metrics

import cats.effect.{IO, Resource}
import io.micrometer.core.instrument.{MeterRegistry, Tag}
import io.micrometer.core.instrument.binder.jvm.{JvmGcMetrics, JvmHeapPressureMetrics, JvmMemoryMetrics, JvmThreadMetrics}
import io.micrometer.core.instrument.binder.system.{DiskSpaceMetrics, ProcessorMetrics}
import io.micrometer.prometheus.{PrometheusConfig, PrometheusMeterRegistry}

import java.io.File
import scala.jdk.CollectionConverters._
object PrometheusMetrics {

  // Use default prometheus config for now
  def registry(): Resource[IO, PrometheusMeterRegistry] =
    Resource.eval {
      for {
        metrics <- prepareRegistry()
        _       <- addBindings(metrics)
      } yield metrics
    }

  private def prepareRegistry(): IO[PrometheusMeterRegistry] =
    IO {
      val meter = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
      meter.config().commonTags(List(Tag.of("app", "$name$")).asJava)
      meter
    }

  private def addBindings(registry: MeterRegistry): IO[Unit] =
    IO {
      new JvmGcMetrics().bindTo(registry)
      new JvmMemoryMetrics().bindTo(registry)
      new JvmThreadMetrics().bindTo(registry)
      new JvmHeapPressureMetrics().bindTo(registry)
      new DiskSpaceMetrics(new File("")).bindTo(registry)
      new ProcessorMetrics().bindTo(registry)
    }
}
