package de.yamuza.tools.vdj_db_exporter;

import static org.fest.assertions.api.Assertions.assertThat;

import org.testng.annotations.Test;

public class MainTest {

	@Test
	public void testFormatComment() throws Exception {
		String expected = "++++      [ipsy]";
		int expectedLenght = expected.length();
		String actual = Main.formatComment("++++[ipsy]");
		assertThat(actual).hasSize(expectedLenght);
		assertThat(actual).isEqualTo(expected);
		
		actual = Main.formatComment("+++ [ipsy]");
		assertThat(actual).hasSize(expectedLenght);
		assertThat(actual).isEqualTo("+++       [ipsy]");
	}
 
}
