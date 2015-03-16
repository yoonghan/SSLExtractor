package com.jaring.jom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SSLExtractor {
	private final String FILE_NAME;
	private final String FILE_EXT;
	private final int MAX_PAGE=200; //create a buffer limit
	private final int TIME_OUT= 30000;
	
	public SSLExtractor(String month, String year, String fileName, String eventTarget, String _type){
		
		this.FILE_NAME = fileName.substring(0, fileName.indexOf("."));
		this.FILE_EXT = fileName.substring(fileName.indexOf(".")+1);
		
		try{
			
			Response con_query = Jsoup.connect("https://www.bca.gov.sg/eService/integ_search.aspx")
					.timeout(TIME_OUT)
					.method(Method.GET)
					.execute();
			Document docConTemp = con_query.parse();
			String _viewState = docConTemp.getElementById("__VIEWSTATE").attr("value");
			String _eventValidation = docConTemp.getElementById("__EVENTVALIDATION").attr("value");
			
			Response con = Jsoup.connect("https://www.bca.gov.sg/eService/integ_search.aspx")
					.data("__EVENTTARGET",eventTarget)
					.data("__EVENTARGUMENT", "")
					.data("__VIEWSTATE",_viewState)
					.data("__EVENTVALIDATION",_eventValidation)
					.timeout(TIME_OUT)
					.method(Method.POST)
					.execute();
			
			Map<String,String> cookies = con.cookies();
			
			//Dumb method, but what can i Do.
			Document doctemp = con.parse();
			_viewState = doctemp.getElementById("__VIEWSTATE").attr("value");
			_eventValidation = doctemp.getElementById("__EVENTVALIDATION").attr("value");
			
		    Document doc = Jsoup.connect("https://www.bca.gov.sg/eService/ListDetails.aspx")
				.data("__VIEWSTATE",_viewState)
				.data("__EVENTVALIDATION",_eventValidation)
				.data("ddlMonth",month)
				.data("ddlYear",year)
				.data("btnSearch","Search")
				.cookies(cookies)
				.timeout(TIME_OUT)
				.post();
			
			Element element = doc.getElementById(_type);

			fileCreator(1,element.html());
			
			System.out.print("page read:1,");
			
			_viewState = doc.getElementById("__VIEWSTATE").attr("value");
			_eventValidation = doc.getElementById("__EVENTVALIDATION").attr("value");
			
			goPageByPageWithCookie(cookies, 1,
					_viewState,
					_eventValidation,
					_type); //estimate pages
			
		}catch(Exception e){
			System.out.println();
			e.printStackTrace();
			System.err.println("Oops hit error in reading the HTML.Error:-"+e.getMessage());
		}
	}
	
	private void goPageByPageWithCookie(Map<String, String> cookies, int pages, String viewState, String validationId, String _type) {
		
		pages++;
				
		try{
			
			System.out.print(""+pages+",");
			
			Document doc = Jsoup.connect("https://www.bca.gov.sg/eService/ShowDetails.aspx")
					.data("__EVENTTARGET",_type)
					.data("__EVENTARGUMENT","Page$"+pages)
					.data("__VIEWSTATE",viewState)
					.data("__EVENTVALIDATION",validationId)
					.cookies(cookies)
					.timeout(TIME_OUT)
					.post();
				
				Element element = doc.getElementById(_type);
				
				fileCreator(pages,element.html());
				
				String _viewState = doc.getElementById("__VIEWSTATE").attr("value");
				String _eventValidation = doc.getElementById("__EVENTVALIDATION").attr("value");

				if(pages < MAX_PAGE){
					//check if the pages can cater this.
					if(element.html().contains("Page$"+(pages+1)) ){
						goPageByPageWithCookie(cookies, pages, _viewState,_eventValidation, _type);
					}	
				}
				
		}catch(Exception e){
			System.out.println();
			System.err.println("File exception."+e.getMessage());
			System.exit(1);
			//e.printStackTrace();
		}
		
	}
	
	
	private void fileCreator(int pageItem, String notes){
		try{
			
			notes="<html><body><table>"+notes+"</table></body></html>";
			
			File file = new File(this.FILE_NAME+"_"+pageItem+"."+this.FILE_EXT);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(notes.getBytes());
			fos.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	private static void checkIfMonthYearValid(String month, String year) {		
		switch(month){
		case "01":case "02":case "03":case "04":case "05":case "06":
		case "07":case "08":case "09":case "10":case "11":case "12":
		break;
		default:
			System.err.println("Month have to be from 01-12");
			System.exit(1);
		}
		
		if(year.length() > 4 || year.length() < 4){
			System.err.println("Year have to be between 2011-20XX");
			System.exit(1);
		}
	}

	private static void checkFolderExist(String fileName) {
		//Check the folder exist.
		try{
			
			fileName = fileName.replaceAll("\\\\", "/"); //make windows to be unix
			
			if(fileName.length() < 4){
				System.err.println("File name:"+fileName+" is invalid");
				System.exit(1);
			}
			if(fileName.contains(".") == false){
				System.err.println("File name:"+fileName+" does not contain a empty file. Please enter example /test/filename.txt");
				System.exit(1);
			}
			
			fileName = fileName.substring(0, fileName.lastIndexOf('/'));
			
			File file = new File(fileName);
			if(file.exists() == false){
				System.err.println("Folder "+fileName+" does not exist.");
				System.exit(1);	
			}
		}catch(Exception e){
			System.err.println("File entered is invalid."+e.getMessage());
			System.exit(1);
		}
	}
	

	public static void main(String args[]){
		
		if(args.length < 3 || args.length > 5){
			System.err.println("Please enter the [month], [year] and [file location + name:"
					+ "\n SSLExtractor [month] [year] [location] [extract]"
					+ "\n E.g. java -cp SSLExtractor.jar com.jaring.jom.SSLExtractor 01 2014 c:/tmp/test.html");
			System.exit(1);
		}
		
		String month = args[0];
		if(month.length() == 1){
			month = "0"+month;
		}
		checkIfMonthYearValid(month,args[1]);
		
		checkFolderExist(args[2]);
		
		String defaultValExtract = (args.length==3 || args[3] == null)? "lnkPermCommBldgWork": args[3];
		String defaultValElement = (args.length==3 || args[4] == null)? "dtgCommBldgWrk": args[4];
		
		new SSLExtractor(month, args[1], args[2], defaultValExtract, defaultValElement);
	}
}
