package com.example.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.DateUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RequestController {

	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
    TableService tableService;
	
	@Autowired
	DataService dataService;
	
	WorkingStatusSingleton wss;
	
	
	private RequestController() {
		wss=WorkingStatusSingleton.getInstance();
	}

	
	@RequestMapping(value="/",method=RequestMethod.GET)
	public String getDefault() {
		return "home";
	}
	
	@RequestMapping(value="/excel",method=RequestMethod.GET)
	public String getExcelUpload() {
		return "excel";
	}
	
	@RequestMapping(value="/database",method=RequestMethod.GET)
	public String getDatabaseView(Model md,RedirectAttributes attributes) {
		
		try {
			List<TablesInserted> tables=tableService.findAll();
			md.addAttribute("tableNames",tables);
			
			
		}catch(Exception e) {
			attributes.addFlashAttribute("message", "Error: Cannot display tables now.");
            return "redirect:/database";
		}
		
		
		
		return "database";
	}
	
	@RequestMapping(value="/tableData/{tablename}",method=RequestMethod.GET)
	public String diplayTableData(Model md,@PathVariable String tablename, RedirectAttributes attributes) {
		TableData td=new TableData();
		
		try {
			List<String> cols=dataService.getColumns(tablename);
			td.setColumns(cols);
			
			List<Map<String, Object>> dataVals=dataService.getData(tablename);
			td.setData(dataVals);
			
			md.addAttribute("allData",td);
			
			
		}catch(Exception e) {
			attributes.addFlashAttribute("message", "Error: Cannot display table data now.");
            return "redirect:/database";
		}
		
		md.addAttribute("tname", tablename);
		return "table_data";
	}
	
	@RequestMapping(value="/uploadfile",method=RequestMethod.POST)
	public String uploadExcelFile(@RequestParam("file") MultipartFile file, RedirectAttributes attributes) {
		 // check if file is empty
        if (file.isEmpty()) {
            attributes.addFlashAttribute("message", "Please select a file to upload.");
            return "redirect:/excel";
        }
        
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        if(!( (fileName.endsWith(".xls")) 
        		|| 
        	 (fileName.endsWith(".xlsx"))
           )) {
        	
        	attributes.addFlashAttribute("message", "Please upload only excel files :" + fileName + '!');

            return "redirect:/excel";
        }
        String oldFileName=fileName;
        
        String tempDir = System.getProperty("java.io.tmpdir");
        if(  !tempDir.endsWith("/") && !tempDir.endsWith( "\\") ) {
            tempDir = tempDir+"/";
    	 // -OR- Path.of(System.getProperty("java.io.tmpdir"))
        }
    	
        
        String randomNumber= ThreadLocalRandom.current().nextInt(0, 1000) +"";
        
        // add Random number to make it unique
        fileName =randomNumber + "_" +   fileName;
        
     // save the file on the local file system
        try {
        
            Path path = Paths.get(tempDir + fileName);
            
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            
        } catch (IOException e) {
            e.printStackTrace();
        	// return success response
        	
            attributes.addFlashAttribute("message", "Error in Uploading file :" + fileName + '!');

            return "redirect:/excel";
        }
        
       
        
        /************ WORK WITH THE EXCEL FILE  ****************/
        int numberOfSheets=0;
        Workbook wb=null;
        String sql="";
        try {
        	
        	File f=new File(tempDir + fileName);
        	
        	wb=WorkbookFactory.create(new FileInputStream(f));
        	
        	numberOfSheets=wb.getNumberOfSheets(); 
        	
        	if(numberOfSheets>1) {
        		attributes.addFlashAttribute("message", "Please upload an excel file with only one sheet. This file has "+ numberOfSheets + " sheets !");
                return "redirect:/excel";
        	}
        	
        	
        	 Sheet sheet = wb.getSheetAt(0);
        	 
        	//get the no of columns which are actually filled with contents
			 int noOfColumns =0;
			 
        	 int firstRowContaingData=0;
			 boolean dataFound=false;
			 
			 for(firstRowContaingData=0;firstRowContaingData<10;firstRowContaingData++){
			   try{
					 noOfColumns=  sheet.getRow(firstRowContaingData).getPhysicalNumberOfCells();
					 dataFound=true;
					 break;
				   }catch(Exception ex) {
					  
				   }
			   }
			    
			 
			   //if header is not in first 10 rows 
			   if(dataFound==false) {
					attributes.addFlashAttribute("message", "Error : The header must be contained in the first 10 rows.  !");
	                return "redirect:/excel";
			   }
			   
			   
			   //get column names 
			   Object columns[]=new Object[noOfColumns];
			   String columnNames="";
				 
			   //get headers -assuming first row to be headers
			   for (int i = 0; i < noOfColumns; i++) {
				   Cell cell1 = sheet.getRow(firstRowContaingData).getCell(i);
				   //defaultTableModel.addColumn(cell1.toString());
				   columns[i]=cell1.toString();
			   }
			   
			   
			   /***************** CREATE THE TABLE  *************/
			    
			    String tableName=fileName.replace(".", "_");
			    sql="DROP TABLE IF EXISTS " + tableName + " ";
			    jdbcTemplate.execute(sql);
			    
			    sql="CREATE TABLE " + tableName + "(";
			   
			    for(int i=0;i<columns.length;i++) {
			    	sql+= "`"  + ((String)columns[i]).trim().replaceAll(" ", "_") + "` " + " VARCHAR(1024) ";
			    	
			    	columnNames+=(String)columns[i];
			    	
			    	if(i<=columns.length-2) { 
			    		sql+=",";
			    		columnNames+=",";
			    	}
			    		
			    }
			    
			    		
			    sql=sql +  ")";
			    
			   
			    
			    jdbcTemplate.execute(sql);
			   
			   /************* INSERT THE FIRST ROW *****************/
			   //get data
			   int lastRowNum=sheet.getPhysicalNumberOfRows();
 			   
			   //to store data
			   String data="";
			   
			   for(int r=firstRowContaingData+1;r<lastRowNum;r++) {
				 
				   sql="insert into " + tableName + " values(";
			      for (int i = 0; i < noOfColumns; i++) {
			    	  Cell cell1 = sheet.getRow(r).getCell(i);
			    	  try {
			    		  switch(cell1.getCellType())
		                    {
		                    case STRING:
		                    	data=cell1.getStringCellValue();
		                        break;
		                    case NUMERIC:
		                    	data=String.format("%.0f",Double.parseDouble(cell1.toString()));
		                    	
		                        break;
		                    case BOOLEAN:
		                    	data=cell1.getBooleanCellValue() +"";
		                     default:
		                    	 if(DateUtil.isCellDateFormatted(cell1)) {
		                    		 data= cell1.getDateCellValue() +"";
		                    	 }else {
		                    		data=cell1.toString();
		                    	 }
		                    }
							   		
			    	  }catch(Exception ex) {
						   data="";
					   }
			    	  finally {
			    		  sql+="'" + data  + "'";
			    		  
			    		  if(i<noOfColumns-1)
			    			  sql+=",";
			    	  }
					
			    	  
				   }
			      sql+=")";
			      jdbcTemplate.execute(sql);
				   		
			   }
			   
			   /************** EXECUTE QUERY TO INSERT DATA ***********/
			   
			   
			   /************** MAKE ENTRY OF TABLE IN MASTER TABLE *******/
			  sql="insert into excel_tables(table_name,column_names) values('" + tableName + "','" + columnNames + "')";
			  System.out.println(sql);
			  jdbcTemplate.execute(sql);
			  
			 
        }
        catch(Exception e) {
        	  attributes.addFlashAttribute("message", "Error : There was an error saving the data in a table in MySQL");
              
              return "redirect:/excel";
        }
        finally {
        	
        	try {
        		wb.close();
        	}catch(Exception ex) {}
        	
        }
        
     // return success response
        
        attributes.addFlashAttribute("message", "You successfully uploaded " + oldFileName + " now named " + fileName + "!  Location: " + tempDir);
        
        return "redirect:/";
	}
	
	
}
