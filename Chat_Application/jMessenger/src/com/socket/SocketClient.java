package com.socket;

import com.ui.ChatFrame;
import java.io.*;
import java.net.*;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class SocketClient implements Runnable{

	public int port;
	public String serverAddr;
	public Socket socket;
	public ChatFrame ui;
	public ObjectInputStream In;
	public ObjectOutputStream Out;
	

	public SocketClient(ChatFrame frame) throws IOException{
		ui = frame; this.serverAddr = ui.serverAddr; this.port = ui.port;
		socket = new Socket(InetAddress.getByName(serverAddr), port);

		Out = new ObjectOutputStream(socket.getOutputStream());
		Out.flush();
		In = new ObjectInputStream(socket.getInputStream());

		
	}

	@Override
	public void run() {
		boolean keepRunning = true;
		while(keepRunning){
			try {
				Message msg = (Message) In.readObject();
				System.out.println("Incoming : "+msg.toString());

				if(msg.type.equals("message")){
					if(msg.recipient.equals(ui.username)){
						ui.jTextArea1.append("["+msg.sender +" > Me] : " + msg.content + "\n");
					}
					else{
						ui.jTextArea1.append("["+ msg.sender +" > "+ msg.recipient +"] : " + msg.content + "\n");
					}

				}
				else if(msg.type.equals("login")){
					if(msg.content.equals("TRUE")){
						ui.jButton2.setEnabled(false); ui.jButton3.setEnabled(false);                        
						ui.jButton4.setEnabled(true); ui.jButton5.setEnabled(true);
						ui.jTextArea1.append("[SERVER > Me] : Login Successful\n");
						ui.jTextField3.setEnabled(false); ui.jPasswordField1.setEnabled(false);
						ui.jButton6.setEnabled(true);
					}
					else{
						ui.jTextArea1.append("[SERVER > Me] : Login Failed\n");
					}
				}
				else if(msg.type.equals("test")){
					ui.jButton1.setEnabled(false);
					ui.jButton2.setEnabled(true); ui.jButton3.setEnabled(true);
					ui.jTextField3.setEnabled(true); ui.jPasswordField1.setEnabled(true);
					ui.jTextField1.setEditable(false); ui.jTextField2.setEditable(false);
					}
				else if(msg.type.equals("newuser")){
					if(!msg.content.equals(ui.username)){
						boolean exists = false;
						for(int i = 0; i < ui.model.getSize(); i++){
							if(ui.model.getElementAt(i).equals(msg.content)){
								exists = true; break;
							}
						}
						if(!exists){ ui.model.addElement(msg.content); }
					}
					
				}
				//mukund- 16 oct 15 --condition on msg type newroom
				else if(msg.type.equals("newroom")||msg.type.equals("joinroom")){
					if(msg.content.equals("TRUE")){
						boolean exists = false;
						for(int i = 0; i < ui.model.getSize(); i++){
							if(ui.model.getElementAt(i).toString().equals("Room :"+msg.sender)){
								exists = true; break;
							}
						}
						if(!exists){ ui.model.addElement("Room :"+msg.sender); }

					}
					else if(msg.content.equals("NOROOM")){
						ui.jTextArea1.append("[SERVER > Me] :No room exists with the given name\n");
					}
					if(msg.content.equals("DUPLICATE")){//create room scenario
						ui.jTextArea1.append("[SERVER > Me]: Room Already Exists ,please choose another name\n");
					}
				}

				else if(msg.type.equals("signup")){
					if(msg.content.equals("TRUE")){
						ui.jButton2.setEnabled(false); ui.jButton3.setEnabled(false);
						ui.jButton4.setEnabled(true); ui.jButton5.setEnabled(true);
						ui.jTextArea1.append("[SERVER > Me] : Signup Successful\n");
					}
					else{
						ui.jTextArea1.append("[SERVER > Me] : Signup Failed\n");
					}
				}

				else if(msg.type.equals("signout")){
					if(msg.content.equals(ui.username)){
						ui.jTextArea1.append("["+ msg.sender +" > Me] : Bye\n");
						ui.jButton1.setEnabled(true); ui.jButton4.setEnabled(false); 
						ui.jTextField1.setEditable(true); ui.jTextField2.setEditable(true);

						for(int i = 1; i < ui.model.size(); i++){
							ui.model.removeElementAt(i);
						}

						ui.clientThread.stop();
					}
					else{
						ui.model.removeElement(msg.content);
						ui.jTextArea1.append("["+ msg.sender +" > All] : "+ msg.content +" has signed out\n");
					}
				}
				
				else{
					ui.jTextArea1.append("[SERVER > Me] : Unknown message type\n");
				}
			}
			catch(Exception ex) {
				keepRunning = false;
				ui.jTextArea1.append("[Application > Me] : Connection Failure\n");
				ui.jButton1.setEnabled(true); ui.jTextField1.setEditable(true); ui.jTextField2.setEditable(true);
				ui.jButton4.setEnabled(false); ui.jButton5.setEnabled(false); ui.jButton5.setEnabled(false);
				ui.jButton6.setEnabled(false);

				for(int i = 1; i < ui.model.size(); i++){
					ui.model.removeElementAt(i);
				}

				ui.clientThread.stop();

				System.out.println("Exception SocketClient run()");
				ex.printStackTrace();
			}
		}
	}

	public void send(Message msg){
		try {
			Out.writeObject(msg);
			Out.flush();
			System.out.println("Outgoing : "+msg.toString());
		} 
		catch (IOException ex) {
			System.out.println("Exception SocketClient send()");
		}
	}

	public void closeThread(Thread t){
		t = null;
	}
}
