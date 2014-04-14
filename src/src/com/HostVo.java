package src.com;

import java.util.Date;

public class HostVo {
	
	private String host;
	private String user;
	private String pass;
	private int port;
	private int sessionTimeOut = 3000;
	private String[] commands;
	public String data;
	
	public HostVo(String hostString){
		String userAndPass = hostString.split("@")[0];
		String hostAndPort = hostString.split("@")[1];
		this.user = userAndPass.split(":")[0];
		this.pass = userAndPass.replace(user + ":", "");
		this.host = hostAndPort.split(":")[0];
		this.setPort(Integer.parseInt(hostAndPort.split(":")[1]));
	}

	@Override
	public String toString() {
		
		String format = "[%d] Host = %s, User = %s, Pass = %s, Port = %d";
		return String.format(format, new Date().getTime(), this.host, this.user, this.pass, this.getPort());
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
	
	public void setPort(int port) {
		this.port = port;
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

	public int getSessionTimeOut() {
		return this.sessionTimeOut;
	}

	public void setSessionTimeOut(int sessionTimeOut) {
		this.sessionTimeOut = sessionTimeOut;
	}
	
}
