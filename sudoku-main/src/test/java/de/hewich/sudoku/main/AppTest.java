package de.hewich.sudoku.main;

import static org.junit.Assert.*;

import org.junit.Test;

import de.helwich.sudoku.main.App;

public class AppTest {

	@Test
	public void testMain() {
		assertEquals("Hell0", App.getHello());
	}

}
