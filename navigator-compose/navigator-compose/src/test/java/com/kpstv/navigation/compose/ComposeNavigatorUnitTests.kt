package com.kpstv.navigation.compose

import com.kpstv.navigation.compose.internals.*
import org.junit.Test

public class ComposeNavigatorUnitTests {
  @Test
  public fun `History Pop & PopUntil Test`() {
    val history = ComposeNavigator.History(TestRoute.key, null, TestRoute.First(""))
    history.push(TestRoute.Second())
    history.push(TestRoute.Second1())
    history.push(TestRoute.Second2())
    history.push(TestRoute.Second3())
    history.push(TestRoute.Second4())
    history.push(TestRoute.Second5())

    assert(history.peek().key is TestRoute.Second5)

    var last = history.pop()!!

    assert(history.peek().key is TestRoute.Second4)
    assert(history.getCurrentRecord().key == last.key)

    last = history.peek()
    history.popUntil(TestRoute.Second::class, inclusive = true)

    assert(history.peek().key is TestRoute.First)
    assert(history.getCurrentRecord().key == last.key)
    assert(history.pop() == null)

    history.push(TestRoute.Second1())
    history.push(TestRoute.Second2())
    history.push(TestRoute.Second3())
    history.push(TestRoute.Second4())
    history.push(TestRoute.Second5())

    history.popUntil(TestRoute.Second2::class, inclusive = false)

    assert(history.peek().key is TestRoute.Second2)

    history.push(TestRoute.Second3())
    history.push(TestRoute.Second4())
    history.push(TestRoute.Second5())

    // jump to root test
    history.popUntil(TestRoute.First::class, inclusive = false)

    assert(history.peek().key is TestRoute.First)
  }

  @Test
  public fun `DialogHistory 'clear' test`() {
    val dialogHistory = ComposeNavigator.History.DialogHistory()
    dialogHistory.createDialogScope(FirstDialog) { false }
    dialogHistory.createDialogScope(SecondDialog) { false }
    dialogHistory.createDialogScope(ThirdDialog) { false }
    dialogHistory.createDialogScope(ForthDialog) { false }

    assert(dialogHistory.getScopes().count() == 4)

    dialogHistory.add(FirstDialog)
    dialogHistory.add(SecondDialog)
    dialogHistory.add(ThirdDialog)
    dialogHistory.add(ForthDialog)

    assert(dialogHistory.peek() is ForthDialog)

    dialogHistory.clear()

    assert(dialogHistory.isEmpty())
  }

  private fun ComposeNavigator.History<*>.push(route: TestRoute) {
    this::class.members.find { it.name == "push" }?.call(this, ComposeNavigator.History.BackStackRecord(route))
  }
}