package com.greenpineyu.fel.optimizer;

import org.testng.annotations.Test;

import com.greenpineyu.fel.Expression;
import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.context.Var;

public class VarVisitOptiTest {

  @Test
  public void VarVisitOpti() {
		FelEngine e = FelEngine.instance;
		Expression exp = e.compile("a+a", null, new VarVisitOpti(
				new Var("a", 1)));
		Object eval = exp.eval((FelContext) null);
		assert ((Number) eval).intValue() == 2;
  }
}
