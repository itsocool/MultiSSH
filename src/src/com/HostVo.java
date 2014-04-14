package src.com;

import java.util.Date;

public class HostVo {
	
	private String host;
	private String user;
	private String pass;
	private int port;
	private String[] commands;
	
	public HostVo(String hostString){
		String userAndPass = hostString.split("@")[0];
		String hostAndPort = hostString.split("@")[1];
		this.user = userAndPass.split(":")[0];
		this.pass = userAndPass.replace(user + ":", "");
		this.host = hostAndPort.split(":")[0];
		this.port = Integer.parseInt(hostAndPort.split(":")[1]);
	}

	@Override
	public String toString() {
		
		String format = "[%d] Host = %s, User = %s, Pass = %s, Port = %d";
		return String.format(format, new Date().getTime(), this.host, this.user, this.pass, this.port);
	}

	public String getHost() {
		return host;
	}

	public String getUser() {
		return user;
	}

	public String getPass() {
		return pass;
	}

	public int getPort() {
		return port;
	}

	public void setCommands(String[] commands) {
		this.commands = commands;
	}

	public String[] getCommands() {
		return commands;
	}
	
}
