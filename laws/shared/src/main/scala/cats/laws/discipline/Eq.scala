package cats
package laws
package discipline

import algebra.Eq
import org.scalacheck.Arbitrary

object eq {

  /**
   * Create an approximation of Eq[A => B] by generating 100 values for A
   * and comparing the application of the two functions.
   */
  implicit def function1Eq[A, B](implicit A: Arbitrary[A], B: Eq[B]): Eq[A => B] = new Eq[A => B] {
    def eqv(f: A => B, g: A => B): Boolean = {
      val samples = List.fill(100)(A.arbitrary.sample).collect{
        case Some(a) => a
        case None => sys.error("Could not generate arbitrary values to compare two functions")
      }
      samples.forall(s => B.eqv(f(s), g(s)) )
    }
  }

  implicit def tuple2Eq[A, B](implicit A: Eq[A], B: Eq[B]): Eq[(A, B)] =
    new Eq[(A, B)] {
      def eqv(x: (A, B), y: (A, B)): Boolean =
        A.eqv(x._1, y._1) && B.eqv(x._2, y._2)
    }

  /**
   * Create an approximation of Eq[Semigroup[A]] by generating values for A
   * and comparing the application of the two combine functions.
   */
  implicit def semigroupEq[A](implicit arbAA: Arbitrary[(A, A)], eqA: Eq[A]): Eq[Semigroup[A]] =
    function1Eq[(A, A), A].on(f =>
      Function.tupled((x, y) => f.combine(x, y))
    )

  implicit def monoidEq[A](implicit eqSA: Eq[Semigroup[A]], eqA: Eq[A]): Eq[Monoid[A]] = new Eq[Monoid[A]] {
    def eqv(f: Monoid[A], g: Monoid[A]): Boolean = {
      eqSA.eqv(f, g) && eqA.eqv(f.empty, g.empty)
    }
  }
}
