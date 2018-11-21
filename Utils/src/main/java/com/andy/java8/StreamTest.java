package com.andy.java8;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;

public class StreamTest {
	public static void main(String[] args) {
		// List<String> list = Stream.of("a", "b", "c")
		// .filter(t -> t.equals("b"))
		// .map(t -> t + "-")
		// .collect(Collectors.toList());
		// System.out.println(list);
		try (FileInputStream in = new FileInputStream("C:\\Users\\RuiLi\\Desktop\\Book1.xls");
				ByteArrayOutputStream out = new ByteArrayOutputStream();) {
			for (byte[] b = new byte[1024]; in.read(b) > 0;) {
				out.write(b);
			}
			String b64 = Base64.getEncoder().encodeToString(out.toByteArray());
			System.out.println(b64);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		;

	}
}
