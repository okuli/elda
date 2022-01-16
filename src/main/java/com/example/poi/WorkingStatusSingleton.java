package com.example.poi;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WorkingStatusSingleton {
	private static WorkingStatusSingleton _instance;
	private boolean isWorking;
	
	//create private constructor
	private WorkingStatusSingleton() {
		isWorking=false;
	}
	
	
	//create singleton
		public static WorkingStatusSingleton getInstance() {
	        if (_instance == null) {                // Single Checked
	            synchronized (WorkingStatusSingleton.class) {
	                if (_instance == null) {        // Double checked
	                    _instance = new WorkingStatusSingleton();
	                }
	            }
	        }
	        return _instance;
		}
	
		
		
		
}
