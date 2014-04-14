package src.com;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHRunner implements Runnable {

	public volatile HostVo host;
	public volatile Session session;
	public volatile Channel channel;
	public volatile InputStream stdOut;
	public volatile InputStream stdErr;
	public volatile PrintStream shellStream;
	public volatile boolean isRunning = false;
	
	public SSHRunner(final Session session, final HostVo host) throws JSchException, IOException{
		this.host = host;
		this.session = session;
		this.session.setConfig("StrictHostKeyChecking", "no");
		this.session.setPassword(this.host.getPass());
		this.session.connect(this.host.getSessionTimeOut());
		this.channel = this.session.openChannel("shell");
	}
	
	@Override
	public void run() {
		try {
			
			OutputStream out = null;
			isRunning = true;
			
			try {
				out = this.channel.getOutputStream();
				this.shellStream = new PrintStream(out, true);
				this.channel.connect();
				this.stdOut = channel.getInputStream();
				this.stdErr = channel.getExtInputStream();
				
				for (String command : host.getCommands()) {
					shellStream.println(command.trim());
				}
				
				if(!"exit".equals(host.getCommands()[host.getCommands().length-1]))
				{
					shellStream.println("exit");
				}
				shellStream.close();
				byte[] byteArray = new byte[4096];
				
				while(this.isRunning)
				{
					if(this.channel.isClosed())
					{
						break;
					}

					while(stdOut.available()>0)
					{
						int i = stdOut.read(byteArray, 0, byteArray.length);
						if( i < 0 ){
							break;
						}
						host.data += new String(byteArray, 0, i);
					}
					
					Thread.sleep(100);
				}

				System.out.println("### [RESULT] === " + host);
				isRunning = false;
				
			} catch (IOException | InterruptedException e) {
				throw new JSchException("Connection Error");
			} finally {
				disconnect();
			}
			
		} catch (JSchException e) {
			System.err.println("### [ERROR] === " + host);
			e.printStackTrace(System.err);
		}
	}
	
	public void disconnect() {
		if(channel != null){
			channel.disconnect();
		}
		if(session != null){
			session.disconnect();
		}
		
		this.channel = null;
		this.session = null;
		isRunning = false;
	}
	
	public void close() {
		try {
			if(stdOut != null) {
				stdOut.close();
			}
			if(stdErr != null) {
				stdErr.close();
			}
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}

	public void dispose() {
		close();
		disconnect();
	}
}
