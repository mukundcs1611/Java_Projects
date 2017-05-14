package com.sort;
//Algorithm
//QUICKSORT(A,p, r)
//1 if(p < r)
//2 	q =PARTITION(A, p, r);
//3 	QUICKSORT(A, p, q - 1);
//4 	QUICKSORT(A, q+1, r);
//
//PARTITION(A, p, r)
//1 x =A[r]
//2 i= p - 1
//3 for j = p to r - 1
//4 if A[j]<=x
//5 	i = i+1
//6 	exchange A[i] with A[j] 
//7 exchange A[i+1] with A[r]

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class QuickSort {
	int partition(int arr[], int low, int high)
	{
		int i = low, j = high;
		int tmp;
		int pivot = arr[(low + high) / 2];

		while (i <= j) {
			while (arr[i] < pivot)
				i++;
			while (arr[j] > pivot)
				j--;
			if (i <= j) {
				tmp = arr[i];
				arr[i] = arr[j];
				arr[j] = tmp;
				i++;
				j--;
			}
		};

		return i;
	}

	void quickSort(int arr[], int low, int high) {
		
		int index = partition(arr, low, high);
		if (low < index - 1)
			quickSort(arr, low, index - 1);
		if (index < high)
			quickSort(arr, index, high);
	}
	public static void main(String[] args){
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
		
		

		QuickSort qs=new QuickSort();
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
		
		qs.quickSort(arr, 0, n-1);
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
