import java.util.ArrayList;
import java.util.Scanner;


public class TowersOfHanoi {
	public static ArrayList<Integer> pole1;
	public static ArrayList<Integer> pole2;
	public static ArrayList<Integer> pole3;
	public enum poles{
		pole1(1),pole2(2),pole3(3);
		public final int id;
		poles(int id) {
			this.id = id;
		}
		public int getId(){
			return id;
		}	  

	}

	public void printCurrent(){
		System.out.println("Pole 1: ");
		for(Integer one: pole1){
			System.out.print(one+" ");
		}
		System.out.println("");
		System.out.println("Pole 2:");
		for(Integer one: pole2){
			System.out.print(one+" ");
		}
		System.out.println("");
		System.out.println("Pole3: ");
		for(Integer one: pole3){
			System.out.print(one+" ");
		}
		System.out.println("");
	}
	void addOrRemoveDisks(boolean addOrRem,int polenum,int diskNum ){
		switch (polenum) {
		case 1:
			if(addOrRem==true)pole1.add(diskNum);
			if(addOrRem==false)pole1.remove(pole1.indexOf(diskNum));	
			break;
		case 2:
			if(addOrRem==true)pole2.add(diskNum);
			if(addOrRem==false)pole2.remove(pole2.indexOf(diskNum));	
			break;
		case 3:
			if(addOrRem==true)pole3.add(diskNum);
			if(addOrRem==false)pole3.remove(pole3.indexOf(diskNum));	
			break;
		default:
			break;
		}

	}
	void TowersOfHanoi(int disknum,int from,int middle,int to){

		if(disknum==1){
			System.out.println("<-----Disk-"+disknum+ " from pole-"+from+" to pole-"+to+" ----->"); 
			addOrRemoveDisks(false,from,disknum);
			addOrRemoveDisks(true,to,disknum);
			printCurrent();
			return;
		}
		else{
			TowersOfHanoi(disknum-1,from,to,middle);
			System.out.println("<-----Disk-"+disknum+ " from pole-"+from+" to pole-"+to+" ----->");
			addOrRemoveDisks(false,from,disknum);
			addOrRemoveDisks(true,to,disknum);
			printCurrent();

			TowersOfHanoi(disknum-1,middle,from,to);
			return;
		}
	}

	public static void main(String[] args){
		System.out.println("Enter the number of disks :");
		Scanner sc = new Scanner(System.in);
		TowersOfHanoi toh=new TowersOfHanoi();
		int i = sc.nextInt();
		
		pole1=new ArrayList<Integer>();
		pole2=new ArrayList<Integer>();
		pole3=new ArrayList<Integer>();
		for(int j=1;j<=i;j++){
			pole1.add(j);	
		}
		toh.printCurrent();
		toh.TowersOfHanoi(i,1,2,3);
	}
}
