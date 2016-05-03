package se.radley.plugin.salat

import play.api.{ Configuration, Environment }
import play.api.inject.{ Binding, Module }

class PlaySalatModule extends Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[PlaySalat].to[PlaySalatImpl])
  }
}