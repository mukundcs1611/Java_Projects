import java.util.Scanner;

public class reverseInPlace {

	public static void main(String[] args){
		Scanner sc=new Scanner(System.in);
		System.out.println("Enter a string");
		
		char[] arr=sc.nextLine().toCharArray();	
		
		
		for(int j = 0, z = arr.length - 1; j < arr.length / 2; j++, z--) {
			    char c = arr[j];
			    arr[j] = arr[z];
			    arr[z] = c;
		}
		 System.out.println("reversed string is ");
		 System.out.print(arr);
		 sc.close();
	}
	
	
	
}
