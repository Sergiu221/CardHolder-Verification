package com.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.controller.ControllerTerminal;
import com.utils.Rule;

public class TestTerminal {

	ControllerTerminal terminal = new ControllerTerminal();
	byte[][] rulesMatrix = { { 0, 50, 1 }, { 51, 100, 2 }, { 101, 127, 3 } };

	@Test
	public void testSelectedRuleAmount40() {
		int amount = 40;
		Rule rule = this.terminal.retrieveSeficiRule(amount, rulesMatrix);
		assertEquals(Rule.Rule_1, rule);
	}

	@Test
	public void testSelectedRuleAmount70() {
		int amount = 70;
		Rule rule = this.terminal.retrieveSeficiRule(amount, rulesMatrix);
		assertEquals(Rule.Rule_2, rule);
	}

	@Test
	public void testSelectedRuleAmount110() {
		int amount = 110;
		Rule rule = this.terminal.retrieveSeficiRule(amount, rulesMatrix);
		assertEquals(Rule.Rule_3, rule);
	}

	
}
