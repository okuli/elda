package com.example.poi;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class TableService {

	//get connection to database from spring boot
	//Connection con;
	//con = jdbcTemplate.getDataSource().getConnection();
	
	@Autowired
    JdbcTemplate jt;

	public List<TablesInserted> findAll() {
        String sql = "select * from excel_tables";
        RowMapper<TablesInserted> rm = new RowMapper<TablesInserted>() {
            @Override
            public TablesInserted mapRow(ResultSet resultSet, int i) throws SQLException {
            	TablesInserted table= new TablesInserted(resultSet.getString("table_name"));
            	
                return table;
            }
        };

        return jt.query(sql, rm);
    }
	
}
