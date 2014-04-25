package com.asokorea;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MultiSSH {

	private static ArrayList<HostVo> hostList;
	private static MultipleShell shell;
	private static String taskName = "_default_";
	private static File taskFile = null;
	private static int maxConnection = 1;
	private static int timeOut = 5 * 1000 ;
	
	public static void main(String[] args) {
		
		long timeSpan = 0;
		
		try {
			
			try {
				taskFile = new File(args[0]);
				taskName = args[1];
			} catch (Exception e) {
				System.err.print("[EXCEPTION] ");
				e.printStackTrace();
			}
			
			System.out.println("[SSH START]");
			
			Document configXml = getXML(taskName);
			String[] commands = getCommand(configXml);
			Path logPath = getLogPath(configXml);
			Date sdt = new Date();
			Date edt = null;
			XPath xpath = XPathFactory.newInstance().newXPath();
			hostList = getHostList(configXml, commands);
			maxConnection = ((Double) xpath.evaluate("//maxConnection", configXml, XPathConstants.NUMBER)).intValue();
			timeOut = ((Double) xpath.evaluate("//timeout", configXml, XPathConstants.NUMBER)).intValue();
			shell = new MultipleShell(hostList, maxConnection, timeOut, logPath);
			shell.executeAll();
			
			if(configXml != null){
				configXml = null;
			}
			
			if(shell != null)
			{
				shell.dispose();
			}
			
			shell = null;
			
			edt = new Date();
			timeSpan = edt.getTime() - sdt.getTime();
			
			synchronized (System.out) {
				showMemory();
				System.out.println("[SSH FINISH] " + timeSpan + "ms");
			}
			
			System.exit(0);
		} catch (XPathExpressionException e) {
			synchronized (System.err) {
				System.err.print("[EXCEPTION] ");
				e.printStackTrace();
			}
			System.exit(1);
		} finally {
			if(shell != null){
				shell.dispose();
			}
		}
	}

	private static Document getXML(String taskName){
		
		Document document = null;
		
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(taskFile.getCanonicalFile());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.err.print("[EXCEPTION] ");
			e.printStackTrace();
		}
		
		return document;
	}
	
	private static String[] getCommand(Document document){
		
		String[] result = null;
		XPath xpath = null;
		NodeList commandNodes = null;
		int length = 0;
		
		try {
			xpath = XPathFactory.newInstance().newXPath();
			commandNodes = (NodeList)xpath.evaluate("//command", document, XPathConstants.NODESET);
			length = commandNodes.getLength();
			result = new String[length];
			
			for (int i = 0; i < length; ++i) {
				Node command = (Node) commandNodes.item(i);
				result[i] = command.getTextContent();
			}
		} catch (XPathExpressionException e) {
			System.err.print("[EXCEPTION] ");
			e.printStackTrace();
		}
		
		return result;
	}
	
	private static ArrayList<HostVo> getHostList(Document configXml, String[] commands){
		
		ArrayList<HostVo> result = null;
		XPath xpath = null;
		NodeList hostNodes = null;
		File hostFile = null;
		Document hostListXml = null;

		try {
			
			xpath = XPathFactory.newInstance().newXPath();
			String exportedHostListFile = (String)xpath.evaluate("//exportedHostListFile", configXml, XPathConstants.STRING);
			hostFile = new File(exportedHostListFile);
			hostListXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(hostFile);
			hostNodes = (NodeList)xpath.evaluate("//row", hostListXml, XPathConstants.NODESET);
			result = new ArrayList<HostVo>();
			
			for (int i = 0; i < hostNodes.getLength(); i++) {
			
				Node row = hostNodes.item(i);
				
				if("row".equals(row.getNodeName())){
					String ip = (String)xpath.evaluate("./col[@number=0]", row, XPathConstants.STRING);
					String user = (String)xpath.evaluate("./col[@number=1]", row, XPathConstants.STRING);
					String pass = (String)xpath.evaluate("./col[@number=2]", row, XPathConstants.STRING);
					
					HostVo vo = new HostVo(ip, user, pass);
					vo.setCommands(commands);
					result.add(vo);
				}
			}
		} catch (SAXException | IOException | ParserConfigurationException | XPathExpressionException e) {
			System.err.print("[EXCEPTION] ");
			e.printStackTrace();
		}
		return result;
	}

	private static Path getLogPath(Document xml) {
		
		Path result = null;
		String logPath = null;
		File file = null;
		XPath xpath = null;

		try {
			xpath = XPathFactory.newInstance().newXPath();
			logPath = (String)xpath.evaluate("//logPath", xml, XPathConstants.STRING);
			file = new File(logPath);
			
			if(file != null && !file.exists()){
				file.mkdir();
			}
			
			result = file.toPath();
		} catch (XPathExpressionException e) {
			System.err.print("[EXCEPTION] ");
			e.printStackTrace();
		}
		return result;
	}
	
	private static void showMemory(){
	    long free  = Runtime.getRuntime().freeMemory();
	    long total = Runtime.getRuntime().totalMemory();
	    long max   = Runtime.getRuntime().maxMemory();

    	System.out.println(String.format("Total Memory : %6.2f MB", (double) total / (1024 * 1024)));
    	System.out.println(String.format("Free  Memory : %6.2f MB", (double) free  / (1024 * 1024)));
    	System.out.println(String.format("Max   Memory : %6.2f MB", (double) max   / (1024 * 1024)));
	}
}
