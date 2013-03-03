package com.example.calibration;

import java.util.ArrayList;
import java.util.List;

public class FeatureTester {
	public static void main(String[] args){
		List<Acceleration> list = new ArrayList<Acceleration>();
		list.add(new Acceleration(1,2,3,(long) 4));
		list.add(new Acceleration(2,3,5,(long) 5));
		list.add(new Acceleration(3,4,1,(long) 6));
		list.add(new Acceleration(1,2,6,(long) 7));
		list.add(new Acceleration(1,2,2,(long) 8));
		list.add(new Acceleration(1,3,2,(long) 9));
		list.add(new Acceleration(1,3,2,(long) 10));
		list.add(new Acceleration(1,3,7,(long) 11));
		list.add(new Acceleration(1,3,2,(long) 12));
		list.add(new Acceleration(1,3,2,(long) 13));
		Feature f = new Feature(list);
		System.out.println(f.getTimePeaks()[2]);
	}
}
