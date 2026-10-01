package example

class Test1 extends munit.FunSuite {
  test("test 1") {
    assert(cats.Eval.later(2).value == 2)
  }
}
