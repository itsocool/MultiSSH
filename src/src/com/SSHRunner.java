package src.com;

import com.jcraft.jsch.Session;

public class SSHRunner implements Runnable {

	public volatile Session session;
	public volatile HostVo host;
	
	public SSHRunner(final Session session, final HostVo host){
		this.session = session;
		this.host = host;
	}
	
	@Override
	public void run() {
		System.out.println(host);
	}

}
