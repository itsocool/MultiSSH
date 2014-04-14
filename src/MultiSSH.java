import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import src.com.HostVo;
import src.com.MultipleShell;

public class MultiSSH {

	private static ArrayList<HostVo> hostList;
	private static MultipleShell shell;
	
	public static void main(String[] args) {

		Date sdt = new Date();
		Date edt = null;

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
			
			HostVo host;
			
			for (String string : hosts) {
				host = new HostVo(string);
				host.setCommands(commands);
				hostList.add(host);
			}
			
			shell = new MultipleShell(hostList, 2);
			shell.executeAll();
			shell.executorService.awaitTermination(3, TimeUnit.SECONDS);
			System.out.println("###################### ShutDown ######################");
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			shell.dispose();
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
