package com.github.jrwest.scalamachine.scalaz

import scalaz.{Monad, Traverse, Applicative}
import com.github.jrwest.scalamachine.core.{HaltRes, ValueRes, Res, EmptyRes, ErrorRes}

package object res {

  implicit val resScalazInstances = new Traverse[Res] with Monad[Res] {
    def point[A](a: => A): Res[A] = ValueRes(a)
    def traverseImpl[G[_],A,B](fa: Res[A])(f: A => G[B])(implicit G: Applicative[G]): G[Res[B]] =
      map(fa)(a => G.map(f(a))(ValueRes(_): Res[B])) match {
        case ValueRes(r) => r
        case HaltRes(c) => G.point(HaltRes(c))
        case ErrorRes(e) => G.point(ErrorRes(e))
        case _ => G.point(EmptyRes)
      }
    def bind[A, B](fa: Res[A])(f: A => Res[B]): Res[B] = fa flatMap f
  }
}