
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;




public class RingElection {

	public static final int Base_Port=1400;
	public static  int  procCount=5;
	public static  int aliveCount=0;

	JFrame w;
	JTextArea textArea;

	public int my_PRIO;
	public static int  highestPriority=0;
	LinkedList<rProcess> ring;
	Messenger ms;
	rProcess me;
	rProcess coordinator;
	boolean I_STARTED_ELECTION;
	boolean I_AM_INITIATOR;
	public static boolean  electionStarted;
	public static boolean isConsumed=false;
	public static boolean iamCoord[];
	public static boolean coordDown=false;
	public static boolean i_consumed_token[];
	static ArrayList<Integer> currentNodes;
	public static int prevCoordId;

	RingElection(){

		currentNodes=new ArrayList<Integer>();
		Collections.synchronizedList(currentNodes);
		JFrame inputFrame=new JFrame();
		JPanel contentPane;
		JTextField textField;
		JTextField textField2;
		JButton bringBack;


		inputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		inputFrame.setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		inputFrame.setContentPane(contentPane);
		JLabel lblNewLabel = new JLabel("Enter No Of Processes:");

		lblNewLabel.setFont(new Font("Tw Cen MT", Font.BOLD, 20));
		contentPane.add(lblNewLabel, "2, 6, right, default");

		lblNewLabel.setFont(new Font("Tw Cen MT", Font.BOLD, 20));

		textField =new JTextField();
		contentPane.add(textField);
		textField.setColumns(10);

		JButton btnNewButton = new JButton("Start Ring");
		contentPane.add(btnNewButton, "4, 8");

		JLabel lbl2 = new JLabel("Bring Back Old Process");
		contentPane.add(lbl2);

		textField2 = new JTextField();
		contentPane.add(textField2);
		textField2.setColumns(10);

		bringBack = new JButton("Bring back");
		contentPane.add(bringBack);
		bringBack.setEnabled(false);
		inputFrame.setVisible(true);

		btnNewButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {

				procCount=Integer.parseInt(textField.getText());

				RingElection.iamCoord=new boolean[procCount];
				RingElection.i_consumed_token=new boolean[procCount];
				for(int i=0;i<procCount;i++){
					RingElection.iamCoord[i]=false;
					RingElection.i_consumed_token[i]=false;
				}
				bringBack.setEnabled(true);

				for(int i=0;i<procCount;i++){
					new RingElection(i);	
				}


			}
		});
		bringBack.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				int process;
				process=Integer.parseInt(textField2.getText());

				new RingElection(process);	



			}
		});





	}
	/*Only use to start the process individually*/



	public RingElection(int myPrio){
		//		currentNodes=new ArrayList<Integer>();
		//	    Collections.synchronizedList(currentNodes);
		this.my_PRIO=myPrio;
		I_AM_INITIATOR=false;
		I_STARTED_ELECTION=false;
		currentNodes.add(myPrio);
		loadProcesses();


		aliveCount=aliveCount+1;

		if(myPrio>=highestPriority){
			highestPriority=myPrio;

		}
		if(highestPriority>=procCount-1){
			iamCoord[highestPriority]=true;
			coordDown=false;
		}
		ms=new Messenger(this); //to listen

		(new Thread(){			//to speak
			@Override
			public void run() {
				// TODO Auto-generated method stub
				w=new JFrame("Process" + me.priority);
				w.setSize(400, 400);
				w.setLayout(new FlowLayout());
				textArea = new JTextArea();
				textArea.setColumns(20);
				textArea.setLineWrap(true);
				textArea.setRows(15);
				textArea.setWrapStyleWord(true);
				JScrollPane scrollPane = new JScrollPane(); 
				scrollPane.setViewportView(textArea);
				scrollPane.setEnabled(true);
				textArea.setEditable(false);
				w.add(textArea);
				JButton btnElect=new JButton("Election");
				JButton btnToken=new JButton("Consume Token");
				JButton btnRelToken=new JButton("Release Token");
				w.add(btnToken);
				w.add(btnRelToken);
				btnRelToken.setEnabled(false);
				if(electionStarted==true){
					btnToken.setEnabled(false);
					btnElect.setEnabled(true);
				}
				w.add(btnElect);
				
				btnElect.setEnabled(false);
				btnToken.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						btnToken.setEnabled(false);
						btnRelToken.setEnabled(true);
						isConsumed=true;


					}
				});

				btnElect.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						// TODO Auto-generated method stub
						
						startElection();
						

					}

				});
				btnElect.addChangeListener(new ChangeListener() {
					
					@Override
					public void stateChanged(ChangeEvent e) {
						// TODO Auto-generated method stub
						if(btnElect.isEnabled()){
							electionStarted=true;
							isConsumed=true;
														
						}
						
						
					}
				});
				btnRelToken.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						// TODO Auto-generated method stub
						btnToken.setEnabled(true);
						btnRelToken.setEnabled(false);
						isConsumed=false;
						i_consumed_token[myPrio]=true;


					}
				});
				w.addWindowListener(new WindowListener() {


					@Override public void windowClosing(WindowEvent e) { 
						//on closing if its the coordinator ; bring back the election button to active state

						aliveCount=aliveCount-1;
						

						if(iamCoord[myPrio]==true){
							iamCoord[myPrio]=false;
							coordDown=true;
							btnToken.setEnabled(false);
							prevCoordId=myPrio;
						}
						Socket socket=null;
						ServerSocket srvSocket=null;
						try{
							socket=new Socket(me.site,me.port);
							socket.setSoTimeout(100);
							
							socket.setReuseAddress(true);
							
							socket.close();
							
						
							
						}
						catch(Exception ex){
							//ex.printStackTrace();
						}
						if(aliveCount==0){System.exit(0);}



					}
					@Override public void windowClosed(WindowEvent e) {
						for(int i:currentNodes){
							if(i==myPrio){
								int occ=currentNodes.indexOf(i);
								currentNodes.remove(occ);
								return;
							}
							
						}
						
					

						if(aliveCount==0){System.exit(0);}
	
										}
					@Override public void windowOpened(WindowEvent e) {
						startElection();
					}
					@Override public void windowIconified(WindowEvent e) {}
					@Override public void windowDeiconified(WindowEvent e) {}
					@Override public void windowActivated(WindowEvent e) {}
					@Override public void windowDeactivated(WindowEvent e) {}
				});


				w.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				w.setVisible(true);
				while(aliveCount!=0){
					// TODO Auto-generated method stub
					if(	isConsumed==true&&i_consumed_token[myPrio]==true){
						btnToken.setEnabled(false);
						btnRelToken.setEnabled(true);

					}
					if(coordDown==true&&electionStarted==false){
						startElection();
						
					}
					if(isConsumed==true&i_consumed_token[myPrio]==false){
						btnToken.setEnabled(false);
					}
					if(coordDown==true&&myPrio==prevCoordId-1){
						btnElect.setEnabled(true);
						btnRelToken.setEnabled(false);
					}
					if(coordDown==true&&electionStarted==false){
						startElection();	
					}
					if(isConsumed==false){
						btnToken.setEnabled(true);
					}
//					if(coordDown==true&&my_PRIO==prevCoordId-1){
//						btnElect.setEnabled(true);
//					}
//					if(electionStarted==true&&!btnElect.isEnabled()){
//						btnElect.setEnabled(true);
//					}
				}
			}
		}).start();
	}

	void loadProcesses(){
		me=new rProcess("localhost",Base_Port+my_PRIO,my_PRIO);
		ring=new LinkedList<rProcess>();
		for(int i=0;i<procCount;i++){
			int round=my_PRIO + i;
			round=round % procCount;
			ring.add(new rProcess("localhost",Base_Port+round,round));
		}
	}

	void startElection(){ //When we want t initiate election
		LinkedList<rProcess> lst=new LinkedList<rProcess>();
		lst.addLast(me);
		I_STARTED_ELECTION=true;
		electionStarted=true;
		if(forwardMessage("E",lst)==false){
			coordinator=me;
			I_STARTED_ELECTION=false;
			System.out.println("Nobody alive. I am cordinator");
			iamCoord[lst.getLast().priority]=true;
			textArea.append("Nobody alive. I am cordinator"+"\n");
		}
	}

	boolean forwardMessage(String msg,LinkedList<rProcess> lst){
		rProcess tmp;
		for(int i=1;i<ring.size();i++){
			tmp=ring.get(i);
			if(!currentNodes.contains((tmp.port-Base_Port))){
				System.out.println("Sending :" + msg + " to " + tmp.port + " failed. Sending to next in ring.");
				textArea.append("Sending :" + msg + " to " + tmp.port + " failed. Sending to next in ring."+"\n");

			}
			else if(ms.send(tmp.site,tmp.port,msg,lst) == true){
				System.out.println("Sent " + msg + " to " + tmp.port);
				textArea.append("Sent " + msg + " to " + tmp.port+"\n");
				return true;
			}
		}	




		return false;
	}

	void receivedEMessage(LinkedList<rProcess> lst){ //predessor sent e msg
		if(I_STARTED_ELECTION == true){
			//find out highest priority
			rProcess max=me;
			for(int i=0;i<lst.size();i++){
				rProcess tmp=lst.get(i);
				if(tmp.priority>max.priority){
					max=tmp;
				}
			}

			lst=new LinkedList<rProcess>();
			lst.add(max);
			I_AM_INITIATOR=true;
			System.out.println("IAM:" + me.port + " Just elected " + max.port);
			textArea.append("IAM:" + me.port + " Just elected " + max.port+"\n");
			coordinator=max;
			forwardMessage("C",lst);
		}else{
			lst.addLast(me);
			forwardMessage("E",lst);
		}
	}

	public void receivedMessage(String msg,LinkedList<rProcess> lst){
		try{Thread.sleep(3000);} catch(Exception ex){ex.printStackTrace();}
		if(msg.equalsIgnoreCase("E")){
			receivedEMessage(lst);
		}else if(msg.equalsIgnoreCase("C")){
			rProcess tmp=lst.get(0);
			coordinator=tmp;
			System.out.println("New Coordinator : " + coordinator.port);

			textArea.append("New Coordinator :" + coordinator.port +"\n" );

			if(!I_AM_INITIATOR){
				forwardMessage("C",lst);
				isConsumed=false;
				
			}
		}
	}

	public static void main(String args[]){

		new RingElection();
	}



}

