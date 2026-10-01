package example

object Main {
  def main(args: Array[String]): Unit = {
    println(cats.Eval.later(2).value)
  }
}
