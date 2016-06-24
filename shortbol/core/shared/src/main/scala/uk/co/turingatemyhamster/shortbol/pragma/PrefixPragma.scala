package uk.co.turingatemyhamster.shortbol
package pragma

import ast._
import ast.sugar._
import ops.Eval.{EvalState, constant, log, withPHooks, withIHooks, withCHooks}
import ops.{EvalContext, FindAll, LogLevel, LogMessage}

import scalaz.Scalaz._
import scalaz._

/**
  * Created by nmrp3 on 24/06/16.
  */
object PrefixPragma {
  def apply: Hook = new Hook {
    var logLevel: LogLevel = LogLevel.Warning
    val findAllIdentifiers = FindAll[Identifier]

    override def register(p: Pragma) = for {
      _ <- withPHooks(pHook)
      _ <- withIHooks(iHook)
      _ <- withCHooks(cHook)
    } yield List(p)

    def pHook(p: Pragma): EvalState[List[Pragma]] = p match {
      case Pragma(LocalName("prefix"), args) =>
        args match {
          case Seq(ValueExp.Identifier(LocalName(pfx)), ValueExp.Identifier(Url(url))) =>
            for {
              _ <- log(LogMessage.info(s"Registered prefix mapping $pfx to $url"))
            } yield List(p)
          case _ =>
            for {
              _ <- log(LogMessage.error(s"Malformed @prefix pragma with $args"))
            } yield Nil
        }
      case _ =>
        constant(List(p))
    }

    def iHook(i: InstanceExp): EvalState[List[InstanceExp]] = for {
      _ <- checkIdentifiers(i)
    } yield List(i)

    def cHook(c: ConstructorDef): EvalState[List[ConstructorDef]] = for {
      _ <- checkIdentifiers(c)
    } yield List(c)

    def checkIdentifiers[T](t: T): EvalState[List[Unit]] =
      (findAllIdentifiers(t) map {
        case QName(NSPrefix(pfx), _) =>
          for {
            found <- gets((_: EvalContext).prgms.get("prefix").to[List].flatten collect {
              case Pragma(_, Seq(ValueExp.Identifier(LocalName(p)), url)) if p == pfx => url
            })
            _ <- if (found.isEmpty) {
              log(LogMessage(s"No prefix binding for $pfx", logLevel, None))
            } else {
              constant(())
            }
          } yield ()
        case _ =>
          constant(())
      }).sequenceU
  }
}