package com.sort;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;




public class HeapSort 
{    
	private static int N;

	public static void heapsort(int arr[])
	{       
		heapify(arr);        
		for (int i = N; i > 0; i--)
		{
			swap(arr,0, i);
			N = N-1;
			maxheap(arr, 0);
		}
	}     

	public static void maxheap(int arr[], int i)
	{ 
		int left = 2*i ;
		int right = 2*i + 1;
		int max = i;
		if (left <= N && arr[left] > arr[i])
			max = left;
		if (right <= N && arr[right] > arr[max])        
			max = right;

		if (max != i)
		{
			swap(arr, i, max);
			maxheap(arr, max);
		}
	}    

	public static void heapify(int arr[])
	{
		N = arr.length-1;
		for (int i = N/2; i >= 0; i--)
			maxheap(arr, i);        
	}

	public static void swap(int arr[], int i, int j)
	{
		int tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp; 
	}    

	public static void main(String[] args) 
	{
		//change n before running
		BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
		String number;
		int n=0;
		System.out.println("Enter the number of elements to be sorted");
		try {
			number=br.readLine();
			n=Integer.parseInt(number);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int arr[] = new int[ n ];


		for (int i = 0; i < n; i++)
			arr[i] = new Random().nextInt(50000000);

		System.out.println("\n<-- Elements before sorting -->");
		for(int i =0;i<n;i++){
			System.out.print(arr[i]+" ");
			if(i%500==0 && i!=0){
				System.out.println("");
			}
		}
		long startTime = System.currentTimeMillis();
		heapsort(arr);	
		long endTime = System.currentTimeMillis();
		System.out.println("\n<-- Elements after sorting -->");        
		for (int i = 0; i < n; i++){
			System.out.print(arr[i]+" ");
			if(i%500==0 && i!=0){
				System.out.println("");
			}
		}

		System.out.println("\nExecution Time: " + (endTime - startTime) + "ms");

	}    
}