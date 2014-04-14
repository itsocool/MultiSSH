package src.com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.invoke.VolatileCallSite;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class MultipleShell {
	private JSch jsch = new JSch();
	private Channel channel;
	private Session session;
	private int port = 22;
	private int sessionTimeOut = 3000;
	private PrintStream shellStream;
	public InputStream stdOut;
	
	public ArrayList<Session> sessions;
	private ArrayList<HostVo> hostList;
	private int maxThread = 8;
	private ExecutorService executorService;
	private volatile int currentPosition = -1;
	private int lastPosition = -1;
	
	public MultipleShell(final ArrayList<HostVo> hostList, final int maxThread){
		
		if(maxThread > 0 && maxThread < 32){
			this.maxThread = maxThread;
		}
		
		this.executorService = Executors.newFixedThreadPool(maxThread);
		this.setHostList(hostList);
		this.lastPosition = hostList.size();
		this.sessions = new ArrayList<Session>(this.maxThread);
		System.out.println("## Create MultipleShell");
	}
	
	public synchronized void executeAll() {
		
		for (final HostVo host : hostList) {
			executorService.execute(new SSHRunner(host));
		}
		
		executorService.shutdown();
	}


	private synchronized HostVo getCurrentHost() {

		while(true){
			if(hostList != null && currentPosition > 0 && lastPosition > 0 && currentPosition <= lastPosition && sessions.size() < maxThread){
				break;
			}
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return hostList.get(currentPosition);
	}

	public void connect(String host, String user, String password)
			throws JSchException {
		connect(host, user, password, this.port, this.sessionTimeOut);
	}

	public void connect(String host, String user, String password, int port)
			throws JSchException {
		connect(host, user, password, port, this.sessionTimeOut);
	}

	public void connect(String host, String user, String password, int port,
			int sessionTimeOut) throws JSchException {
		this.session = this.jsch.getSession(user, host, port);
		this.session.setConfig("StrictHostKeyChecking", "no");
		this.session.setPassword(password);
		this.session.connect(sessionTimeOut);
		this.channel = this.session.openChannel("shell");

		OutputStream out = null;
		try {
			out = this.channel.getOutputStream();
		} catch (IOException e) {
			throw new JSchException("Connection Error");
		}

		this.shellStream = new PrintStream(out, true);
		this.channel.connect();
	}

	public void execute(String[] commandSet) throws InterruptedException, IOException
  {
    for (String command : commandSet) {
      this.shellStream.println(command.trim());
    }

    if (!("exit".equals(commandSet[(commandSet.length - 1)])))
    {
      this.shellStream.println("exit");
    }
    this.shellStream.close();
    this.stdOut = this.channel.getInputStream();

    while (!(this.channel.isClosed()))
    {
      Thread.sleep(10L);
    }
  }

	public void disconnect() {
		if (this.stdOut != null) {
			this.stdOut = null;
		}

		if (this.channel != null) {
			this.channel.disconnect();
		}
		if (this.session != null)
			this.session.disconnect();
	}

	public ArrayList<HostVo> getHostList() {
		return hostList;
	}

	public void setHostList(ArrayList<HostVo> hostList) {
		this.hostList = hostList;
	}

}