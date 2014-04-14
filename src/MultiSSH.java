import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import src.com.HostVo;
import src.com.MultipleShell;
import sun.misc.Launcher;
import asokorea.ShellHelper;

import com.jcraft.jsch.JSchException;


public class MultiSSH {

	private static ArrayList<HostVo> hostList;
	private static Map<String, Object> taskMap;
	private static MultipleShell shell;
	
	public static void main(String[] args) {
		
		String[] hosts = args[0].split(";");
		hostList = new ArrayList<HostVo>();
		String[] commands = new String[]{
				"en",
				"ter len 0",
				"sh clock",
				"sh run",
				"sh ver",
				"exit"
		};
		
		HostVo host;
		
		for (String string : hosts) {
			host = new HostVo(string);
			host.setCommands(commands);
			hostList.add(host);
		}

		shell = new MultipleShell(hostList, 2);
		shell.executeAll();
		
		showMemory();
/*		
	    ShellHelper shell = new ShellHelper();

	    String host = args[0];
	    String user = args[1];
	    String pass = args[2];
	    int port = Integer.valueOf(args[3]).intValue();
	    String path = args[4];
	    File commandFile = null;
	    File file = new File(path);

	    if ((file != null) && (file.isFile()) && (file.exists()))
	    {
	      commandFile = file;
	    } else {
	      String baseDir = Launcher.class.getResource("/").getPath();
	      String url = baseDir + path;

	      file = new File(url);

	      if ((file != null) && (file.isFile()) && (file.exists()))
	      {
	        commandFile = file;
	      }
	    }

	    ArrayList commands = null;
	    String[] commandStrings = null;
	    try { commands = new ArrayList();

	      Object localObject1 = null; Object localObject4 = null;
	      BufferedReader br;
	      Object localObject3;
	      try { br = new BufferedReader(new FileReader(commandFile));
	      }
	      finally
	      {
	        localObject3 = localThrowable; break label266: label266: if (localObject3 != localThrowable) localObject3.addSuppressed(localThrowable);
	      }
	      commandStrings = (String[])commands.toArray(new String[commands.size()]);

	      shell.connect(host, user, pass, port);
	      shell.execute(commandStrings);
	      InputStream stdOut = shell.stdOut;
	      String result = "";
	      byte[] byteArray = new byte[4096];

	      while (stdOut.available() > 0)
	      {
	        int i = stdOut.read(byteArray, 0, byteArray.length);
	        if (i < 0) {
	          break;
	        }
	        result = result + new String(byteArray, 0, i);
	      }

	      System.out.print(result);
	      System.exit(0);
	    } catch (JSchException e1) {
	      e1.printStackTrace();
	      System.exit(1);
	    } catch (java.io.IOException e2) {
	      e2.printStackTrace();
	      System.exit(2);
	    } finally {
	      shell.disconnect();
	    }
	    */
	}

	private static void showMemory(){
	    long free  = Runtime.getRuntime().freeMemory();
	    long total = Runtime.getRuntime().totalMemory();
	    long max   = Runtime.getRuntime().maxMemory();

	    System.out.format("Total Memory : %6.2f MB%n", (double) total / (1024 * 1024));
	    System.out.format("Free  Memory : %6.2f MB%n", (double) free  / (1024 * 1024));
	    System.out.format("Max   Memory : %6.2f MB%n", (double) max   / (1024 * 1024));
	}
}
