package com.example.poi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class TableData {

	private List<String> columns;
	private List<Map<String,Object>> data;
	
	
	public TableData() {
		columns=new ArrayList<>();
		data=new ArrayList<>();
	}
	
	
}
