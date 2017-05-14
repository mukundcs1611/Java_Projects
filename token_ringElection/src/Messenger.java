

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;


public class Messenger implements Runnable{
	RingElection r;

	
	public Messenger(RingElection r) {
		this.r=r;
		new Thread(this).start();
	}
	
	public boolean send(String site, int port, String msg, LinkedList<rProcess> lst){
		Socket s=null;
		boolean done=false;
		try{
			s=new Socket(site,port);
			ObjectOutputStream obos=new ObjectOutputStream(s.getOutputStream());
			obos.writeUTF(msg);
			obos.writeObject(lst);
			obos.flush();
			obos.reset();
			done=true;
			s.close();
		}catch(IOException ie){
			ie.printStackTrace();
		}
		return done;
	}

	@Override
	public void run() {
		ServerSocket srv;
		try {
			srv = new ServerSocket(r.me.port);
			srv.setReuseAddress(true);
			Socket s=null;
			String msg=null;
			LinkedList<rProcess> lst=null;
			ObjectInputStream obis=null;
			while(true){
				try{
					s=srv.accept();
					obis=new ObjectInputStream(s.getInputStream());
					msg=obis.readUTF();
					lst = (LinkedList<rProcess>) obis.readObject();
					r.receivedMessage(msg,lst);
				}catch(IOException | ClassNotFoundException ie){
					try{
						s.shutdownInput();
						s.shutdownOutput();
						s.setSoTimeout(10);
						s.close();
						srv.close();
						}
					catch(Exception ex){
						//
					}
						
					//ie.printStackTrace();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}