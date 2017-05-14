package com.graphs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class Solution {
	public static void main(String[] args){
		Scanner sc=new Scanner(System.in);
		int noOfTestCases=sc.nextInt();
		while(noOfTestCases!=0){
		  int size =sc.nextInt();
		  int arr[]=new int[size];
		  
		  
		  int maxsofar;
		  int maxCurrent;
		  for(int i=0;i<size;i++){
			  arr[i]=sc.nextInt();
		  }
		  boolean allNegative=true;
		  //Arrays.sort(arr);
		  maxsofar=arr[0];
		  maxCurrent=arr[0];
		  int count=0;
		  for(int i=1;i<size;i++){
			  //maxCurrent=arr[i];
			  maxCurrent=Math.max(arr[i],maxCurrent+arr[i]);
			  maxsofar=Math.max(maxsofar, maxCurrent);
			  if(arr[i]>0)allNegative=false;
		  }
		  Arrays.sort(arr);
		  for(int i=0;i<size;i++){
			  if(!allNegative){
				  if( arr[i]>0){
				  count+=arr[i];
				  }
			  }
			  else
				  count+=arr[i];
				  count=Math.max(count,arr[i]);
			  
		  }
		 
		  System.out.print(maxsofar+" "+count);
		  System.out.println("");
		  noOfTestCases--;
		}
		sc.close();	
	}
	
}