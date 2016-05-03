package se.radley.plugin.salat

import play.api.Configuration
import play.api.Environment
import play.api.inject.Binding
import play.api.inject.Module

class SalatModule extends Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[SalatComponent].to[SalatComponentImpl])
  }
}