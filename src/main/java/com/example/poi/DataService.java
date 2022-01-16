package com.example.poi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class DataService {

	
	@Autowired
	JdbcTemplate jt;
	
	
	
	public List<String> getColumns(String tableName) throws Exception {
        String sql = "select column_names from excel_tables where table_name='"  + tableName  + "'";
        
        List<String> lstCols=new ArrayList<>();
        
        String cols =jt.queryForObject(sql, String.class);
        
        String colParts[]=cols.split(",");
        
        for(int i=0;i<colParts.length;i++) {
        	
        	lstCols.add(colParts[i]);
        	
        }
        
        
       return lstCols;
       
	}
	
	
	public List<Map<String, Object>> getData(String tableName) throws Exception {
        String sql = "select * from "  + tableName  + "";
        
        List<Map<String, Object>> lstData=jt.queryForList(sql);
        
       return lstData;
       
	}
}
