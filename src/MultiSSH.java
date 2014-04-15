import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import sun.misc.Launcher;

import com.asokorea.HostVo;
import com.asokorea.MultipleShell;

public class MultiSSH {

	private static ArrayList<HostVo> hostList;
	private static MultipleShell shell;
	
	public static void main(String[] args) {

		Date sdt = new Date();
		Date edt = null;
		String basePath = Launcher.class.getResource("/").getPath();

		try {
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
			
			File file = new File(basePath + "logs");
			
			if(file != null && !file.exists()){
				file.mkdir();
			}
			
			HostVo vo;
			
			for (String string : hosts) {
				if(string != null && string.trim().length() > 0){
					String userAndPass = string.split("@")[0];
					String hostAndPort = string.split("@")[1];
					String user = userAndPass.split(":")[0];
					String pass = userAndPass.replace(user + ":", "");
					String host = hostAndPort.split(":")[0];
					int port = Integer.parseInt(hostAndPort.split(":")[1]);
					Path resultPath = Paths.get(basePath, "logs");
					
					vo = new HostVo(host, user, pass, commands, resultPath, port);
					hostList.add(vo);
				}
			}
			
			shell = new MultipleShell(hostList, 16);
			
			shell.logDir = file.getAbsolutePath();
			shell.executeAll();
			shell.executorService.awaitTermination(3, TimeUnit.SECONDS);
			System.out.println("###################### ShutDown ######################");
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			if(shell != null)
			{
				shell.dispose();
			}
			
			shell = null;
			
			showMemory();
			edt = new Date();
			System.out.println(Long.valueOf(edt.getTime() - sdt.getTime()) + "ms");
			System.out.println("###################### finish ######################");
			System.exit(0);
		}
		
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
