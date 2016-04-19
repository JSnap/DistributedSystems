package activitystreamer.client;

import java.awt.Component;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import activitystreamer.util.Settings;

public class ClientSolution extends Thread {
	private static final Logger log = LogManager.getLogger();
	private static ClientSolution clientSolution;
	private TextFrame textFrame;
	
	/*
	 * additional variables
	 */
	private DataInputStream in;
	private DataOutputStream out;
	private BufferedReader inreader;
	private PrintWriter outwriter;
	private Socket clientSocket;
	private boolean isRun=false;
	private JSONParser parser = new JSONParser();
	// this is a singleton object
	public static ClientSolution getInstance(){
		if(clientSolution==null){
			clientSolution = new ClientSolution();
		}
		return clientSolution;
	}
	
	public ClientSolution(){
		/*
		 * some additional initialization
		 */
		try {
			InetAddress addr = InetAddress.getByName(Settings.getRemoteHostname());
			int port=Settings.getRemotePort();
			clientSocket=new Socket(addr,port);
			in = new DataInputStream(clientSocket.getInputStream());			
			out=new DataOutputStream(clientSocket.getOutputStream());
			
			inreader=new BufferedReader(new InputStreamReader(in));
			outwriter = new PrintWriter(out, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			inreader=null;
			outwriter=null;
			return;
		}
		

		// open the gui
		log.debug("opening the gui");
		textFrame = new TextFrame();
		// start the client's thread
		start();
		isRun=true;
	}
	
	// called by the gui when the user clicks "send"
	public void sendActivityObject(JSONObject activityObj){
		if(isRun&&outwriter!=null)
		{
			outwriter.println(activityObj.toJSONString());
			outwriter.flush();
		}
	}
	
	// called by the gui when the user clicks disconnect
	public void disconnect(){
		textFrame.setVisible(false);
		/*
		 * other things to do
		 */
		isRun=false;
	}
	

	// the client's run method, to receive messages
	@Override
	public void run(){
		while(isRun && inreader != null)
		{
			try {
				String revStr=inreader.readLine();
				JSONObject obj;
				try {
					obj = (JSONObject) parser.parse(revStr);
					textFrame.setOutputText(obj);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					log.error("invalid received JSON object entered into input text field");
					isRun=false;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.error("receive data failed");
				isRun=false;
			}
			
		}
		try {
			in.close();
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("close socket failed");
		}
		
	}

	/*
	 * additional methods
	 */
	
}
