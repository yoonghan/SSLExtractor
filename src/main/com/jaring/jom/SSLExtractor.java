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

	public SSLExtractor(String month, String year, String fileName){
		
		this.FILE_NAME = fileName.substring(0, fileName.indexOf("."));
		this.FILE_EXT = fileName.substring(fileName.indexOf(".")+1);
		
		try{
			Response con = Jsoup.connect("https://www.bca.gov.sg/eService/integ_search.aspx")
					.data("__EVENTTARGET","lnkPermCommBldgWork")
					.data("__EVENTARGUMENT", "")
					.data("__VIEWSTATE","/wEPDwULLTE4OTcyMDg1NTlkZHxjVeWskxF6vz2kPZIJRpnyyhqr")
					.data("__EVENTVALIDATION","/wEWBwLcyP7tDwK918TCBwLXkej0AwKh0MbqDQLs0dCqDwLr3JnzDQLu2dPaD82I7EgzdItJ/gV2+Rj4bN56IWNp")
					.timeout(30000)
					.method(Method.POST)
					.execute();
			
			System.out.println("COOKIE:-"+con.cookies());
			Map<String,String> cookies = con.cookies();
			
			Document doc = Jsoup.connect("https://www.bca.gov.sg/eService/ListDetails.aspx")
				.data("__VIEWSTATE","/wEPDwUJNTc1NzYwMjQ5D2QWBgIDDw8WAh4EVGV4dAU2TGlzdGluZyBvZiBQZXJtaXRzIHRvIENvbW1lbmNlIFN0cnVjdHVyYWwgV29ya3MgSXNzdWVkZGQCBQ8PFgIfAAU2TGlzdGluZyBvZiBQZXJtaXRzIHRvIENvbW1lbmNlIFN0cnVjdHVyYWwgV29ya3MgSXNzdWVkZGQCBw9kFgQCAw8QDxYGHg1EYXRhVGV4dEZpZWxkBQpNb250aF9OYW1lHg5EYXRhVmFsdWVGaWVsZAUITW9udGhfSUQeC18hRGF0YUJvdW5kZ2QQFQwHSmFudWFyeQdGZWJ1YXJ5BU1hcmNoBUFwcmlsA01heQRKdW5lBEp1bHkGQXVndXN0CVNlcHRlbWJlcgdPY3RvYmVyCE5vdmVtYmVyCERlY2VtYmVyFQwCMDECMDICMDMCMDQCMDUCMDYCMDcCMDgCMDkCMTACMTECMTIUKwMMZ2dnZ2dnZ2dnZ2dnZGQCBw8QZA8WAmYCARYCEAUEMjAxNAUEMjAxNGcQBQQyMDEzBQQyMDEzZ2RkZEQCUZiLPhyLs6am6wHb/tpEiMSo")
				.data("__EVENTVALIDATION","/wEWEAL5z6nZDQLv7IT3BALv7ID3BALv7Lz3BALv7Lj3BALv7LT3BALv7LD3BALv7Kz3BALv7Oj0BALv7OT0BALw7Ij3BALw7IT3BALw7ID3BAL4wbLkBAL4wYbfAwKln/PuCiCKititrmJg6cAsjB0iY2jbCGSu")
				.data("ddlMonth",month)
				.data("ddlYear",year)
				.data("btnSearch","Search")
				.cookies(cookies)
				.timeout(30000)
				.post();
			
			Element element = doc.getElementById("dtgCommBldgWrk");

			fileCreator(1,element.html());
			
			System.out.print("page read:1,");
			
			goPageByPageWithCookie(cookies, 1,
					"/wEPDwUKMTc4NzE0MDAwNw9kFgYCAw8PFgIeBFRleHQFNkxpc3Rpbmcgb2YgUGVybWl0cyB0byBDb21tZW5jZSBTdHJ1Y3R1cmFsIFdvcmtzIElzc3VlZGRkAgUPDxYCHwAFNkxpc3Rpbmcgb2YgUGVybWl0cyB0byBDb21tZW5jZSBTdHJ1Y3R1cmFsIFdvcmtzIElzc3VlZGRkAgcPZBYSAgMPDxYCHwAFAjAxZGQCBg8PFgIfAAUEMjAxNGRkAggPDxYCHwAFJFNlYXJjaCBSZXN1bHQgOiA1MzcgcmVjb3JkKHMpIGZvdW5kIWRkAgoPPCsADQBkAgwPPCsADQBkAg4PPCsADQBkAhAPPCsADQEADxYEHgtfIURhdGFCb3VuZGceC18hSXRlbUNvdW50ApkEZBYCZg9kFhYCAg9kFggCAQ8PFgIfAAWAAlBST1BPU0VEIENPTkRPTUlOSVVNIERFVkVMT1BNRU5UIENPTVBSSVNJTkcgT0YgMiBCTE9DS1MgT0YgMzAtU1RPUkVZIEFQQVJUTUVOVCBGTEFUIChUT1RBTCAxNzUgVU5JVFMpIFdJVEggUFJPVklTSU9OIEZPUiAxIEJMT0NLIE9GIDYtREVDS1MgT0YgTVVMVEktU1RPUkVZIENBUiBQQVJLLCAyIExFVkVMUyBTS1kgVEVSUkFDRVMsIFNXSU1NSU5HIFBPT0wgQU5EIE9USEVSIENPTU1VTkFMIEZBQ0lMSVRJRVMgT04gTE9UIDgyN0EgVFMgMjggQVQgS0hkZAIDDw8WAh8ABV1BTExBTiBMSVFVSUNJQSBHVUlMSU5HICwgS09IIEJST1RIRVJTIEJVSUxESU5HICZhbXA7IENJVklMIEVOR0lORUVSSU5HIENPTlRSQUNUT1IgKFBURS4pIExURC5kZAIEDw8WAh8ABQtOZyBTb29uIEh1YWRkAgUPDxYCHwAFE0xPVyBLT05HIFlFTiBTVEVWRU5kZAIDD2QWCAIBDw8WAh8ABY4BUFJPUE9TRUQgQURESVRJT05TIEFORCBBTFRFUkFUSU9OUyBUTyBFWElTVElORyBTSU5HTEUgU1RPUkVZIEZBQ1RPUlkgSU5WT0xWSU5HIFRIRSBBRERJVElPTiBPRiBPVkVSSEVBRCBDUkFORVMgT04gTE9UIDA1OTNWIE1LMDcgQVQgNyBHVUwgTEFORWRkAgMPDxYCHwAFNVFVQU4gWUlORyBTQVUgLCBURU8gJmFtcDsgSE9ORyBDT05TVFJVQ1RJT04gUFRFLiBMVEQuZGQCBA8PFgIfAAULVEFJIFRBTiBZSU5kZAIFDw8WAh8ABQtUQUkgVEFOIFlJTmRkAgQPZBYIAgEPDxYCHwAFmQFQUk9QT1NFRCBSRVBMQUNFTUVOVCBPRiBFWElTVElORyBGVUxMLUhFSUdIVCBHTEFTUyBQQU5FTCBBVCBVTklUICMwOC0yMCBDT1NUQSBERUwgU09MIENPTkRPTUlOSVVNIE9OIExPVCAxMDA5MUMgTUsgMjcgQVQgNzYgQkFZU0hPUkUgUk9BRCBTSU5HQVBPUkUgNDY5OTBkZAIDDw8WAh8ABSRURU5HIFNJT0sgS0hJTSBKQVNMSU5FICwgU0tMIFBURSBMVERkZAIEDw8WAh8ABQ9LT05HIEtBTSBDSEVPTkdkZAIFDw8WAh8ABQYmbmJzcDtkZAIFD2QWCAIBDw8WAh8ABVdQUk9QT1NFRCBDT05ET01JTklVTSBERVZFTE9QTUVOVCBBVCAxIEpBTEFOIFJFTUFKQSBPTiBMT1QgMDE0OThUIE1LMTAgQVQgMSBKQUxBTiBSRU1BSkFkZAIDDw8WAh8ABS5LV0FZIEpJTiBURUNLICwgQkVORyBTSUVXIENPTlNUUlVDVElPTiBQVEUgTFREZGQCBA8PFgIfAAUNTkdBTiBTRUUgUFlOR2RkAgUPDxYCHwAFD01PSEFOIFNIQU5NVUdBTWRkAgYPZBYIAgEPDxYCHwAFnAFDT05UUkFDVCA5MjlBIC0gQ09OU1RSVUNUSU9OIEFORCBDT01QTEVUSU9OIE9GIFRVTk5FTFMgQkVUV0VFTiBVQkkgQU5EIEtBS0kgQlVLSVQgU1RBVElPTlMgQU5EIFJFQ0VQVElPTiBUVU5ORUxTIEZPUiBET1dOVE9XTiBMSU5FIFNUQUdFIDMgQVQgVEVNUCBST0FEIE5BTUVkZAIDDw8WAh8ABTJLT1lBTUEgVE9TSElLSSAsIE5JU0hJTUFUU1UgQ09OU1RSVUNUSU9OIENPLiwgTFRELmRkAgQPDxYCHwAFG0NIVUEgS09ORyBZRU9XLExBVSBXRUkgSElOR2RkAgUPDxYCHwAFBiZuYnNwO2RkAgcPZBYIAgEPDxYCHwAFgAJQUk9QT1NFRCBFUkVDVElPTiBPRiBBIFNJTkdMRS1TVE9SRVkgR0VORVJBTCBJTkRVU1RSSUFMIEZBQ1RPUlkgRk9SIFNPUlRJTkcgWUFSRCBGT1IgUkVDWUNMRSBNQVRFUklBTFMgQU5EIEEgMy1TVE9SRVkgQU5ORVggQlVJTERJTkcgV0lUSCBURU1QT1JBUlkgQU5DSUxMQVJZIFNUQUZGIENBTlRFRU4sIEFOQ0lMTEFSWSBPRkZJQ0UgQU5EIFRFTVBPUkFSWSBBTkNJTExBUlkgV09SS0VSUycgRE9STUlUT1JZIChGT1IgMTQgV09SS0VSUykgT04gTE9UZGQCAw8PFgIfAAUmU0FNIFNIRUUgQ0hPT05HICwgQkQgQ1JBTkVURUNIIFBURSBMVERkZAIEDw8WAh8ABQ9DSEVBTkcgSkVOIEJPT05kZAIFDw8WAh8ABQ1XT05HIFNJRVcgV0FIZGQCCA9kFggCAQ8PFgIfAAWDAVBST1BPU0VEIFJFQ09OU1RSVUNUSU9OIFRPIEFOIEVYSVNUSU5HIDItU1RPUkVZIFNFTUktREVUQUNIRUQgRFdFTExJTkcgSE9VU0UgV0lUSCBBIE5FVyBBVFRJQyBPTiBMT1QgMDUyMzRYIE1LMTggQVQgNDAgSkFMQU4gVEFNQlVSZGQCAw8PFgIfAAU3U0lWQUxJTkdBTSBSQUpNT0hBTiAsIFdPUktTSE9QIEkuRC4gJmFtcDsgQlVJTEQgUFRFIExURGRkAgQPDxYCHwAFDU5HIERJQ0sgWU9VTkdkZAIFDw8WAh8ABRVLT0sgRU5HIFRJT05HIERFU01PTkRkZAIJD2QWCAIBDw8WAh8ABdYBUFJPUE9TRUQgRVJFQ1RJT04gT0YgQSA2LVNUT1JFWSBNVUxUSS1VU0VSIFJFU0VBUkNIICZhbXA7IERFVkVMT1BNRU5UIEJVSUxESU5HIFdJVEggQSBCQVNFTUVOVCBDQVIgUEFSSyBBVCBDTEVBTlRFQ0ggVklFVyAvIENMRUFOVEVDSCBMT09QIChXRVNURVJOIFdBVEVSIENBVENITUVOVCkgT04gTE9UIDAxNzU0ViAmYW1wOyAwMTc3M1QgTUswOSBBVCBDTEVBTlRFQ0ggTE9PUGRkAgMPDxYCHwAFKExJRSBUSk8gTkdBSyAsIFNBTkNIT09OIEJVSUxERVJTIFBURSBMVERkZAIEDw8WAh8ABRBMRU9ORyBCT09OIENIRU5HZGQCBQ8PFgIfAAUPVE9ORyBDSEVSTkcgWUFXZGQCCg9kFggCAQ8PFgIfAAWrAlBST1AgQ09ORE8gSE9VU0lORyBERVZFTE9QTUVOVCBDT01QUklTSU5HIDEgQkxLIE9GIDUtU1RZLCAxIEJMS1MgT0YgNi1TVFksIDIgQkxLUyBPRiA3LVNUWSwgMiBCTEtTIE9GIDgtU1RZLCAyIEJMS1MgT0YgOS1TVFksIDMgQkxLUyBPRiAxMC1TVFkgUkVTSURFTlRJQUwgRkxBVFMgKFRPVEFMIDM2NikgV0lUSCBCQVNFTUVOVCBDQVJQQVJLICZhbXA7IEFOQ0lMTEFSWSBGQUNJTElUSUVTIEFUIEtFUFBFTCBCQVkgRFJJVkUgKEJVS0lUIE1FUkFIIFBMQU5OIE9OIExPVCAwMzQ5M1QgTUswMSBBVCBLRVBQRUwgQkFZIERSSVZFZGQCAw8PFgIfAAUqWU9ORyBUSUFNIFlPT04gLCBXT0ggSFVQIChQUklWQVRFIExJTUlURUQpZGQCBA8PFgIfAAUMTElNIEtFTiBDSEFJZGQCBQ8PFgIfAAUGJm5ic3A7ZGQCCw9kFggCAQ8PFgIfAAV0UFJPUE9TRUQgTkVXIEVSRUNUSU9OIE9GIEEgMi1TVE9SRVkgU0VNSS1ERVRBQ0hFRCBEV0VMTElORyBIT1VTRSBXSVRIIEFOIEFUVElDIE9OIExPVCAxMjZWIE1LIDE1IEFUIDcxIFRIT01TT04gUklER0VkZAIDDw8WAh8ABSlNT05JTkEgTS5DQUJFUiAsIE1BTklGSUVMRCBERVNJR04gQ09OU1VMVGRkAgQPDxYCHwAFDU9IIENIT09OIENIWUVkZAIFDw8WAh8ABQYmbmJzcDtkZAIMDw8WAh4HVmlzaWJsZWhkZAIRDzwrAA0AZAITDzwrAA0AZBgGBQ5kdGdDb21tQmxkZ1dyaw88KwAKAQgCNmQFBmR0Z1RPUA9nZAUQZHRnQ0RTaGVsdGVyUGxhbg9nZAULZHRnQmxkZ1BsYW4PZ2QFDWR0Z1N0cnVjdFBsYW4PZ2QFBmR0Z0NTQw9nZH00Wn5Mh0fe6gamBDd3pzVGSAzj",
					"/wEWGgL0qNpnAsPlwfgCAsPlvfgCAsPlqfgCAsPlpfgCAsPlsfgCAsPlrfgCAsPlmfgCAsPllfgCAq3v8cgDAsbG07UGAqvVxoMNAs6a2MIJAp7WmqQFAvzuiSQCw+XB+AICw+W9+AICw+Wp+AICw+Wl+AICw+Wx+AICw+Wt+AICw+WZ+AICw+WV+AICre/xyAMCxsbTtQYCpZ/z7gqJlK98e+N/ylZLFcwos0T64EDHHg=="
					); //estimate pages
			
		}catch(Exception e){
			System.out.println();
			System.err.println("Oops hit error in reading the HTML.Error:-"+e.getMessage());
		}
	}
	
	private void goPageByPageWithCookie(Map<String, String> cookies, int pages, String viewState, String validationId) {
		
		pages++;
				
		try{
			
			System.out.print(""+pages+",");
			
			Document doc = Jsoup.connect("https://www.bca.gov.sg/eService/ShowDetails.aspx")
					.data("__EVENTTARGET","dtgCommBldgWrk")
					.data("__EVENTARGUMENT","Page$"+pages)
					.data("__VIEWSTATE",viewState)
					.data("__EVENTVALIDATION",validationId)
					.cookies(cookies)
					.timeout(30000)
					.post();
				
				Element element = doc.getElementById("dtgCommBldgWrk");
				
				fileCreator(pages,element.html());
				
				String _viewState = doc.getElementById("__VIEWSTATE").attr("value");
				String _eventValidation = doc.getElementById("__EVENTVALIDATION").attr("value");

				if(pages < MAX_PAGE){
					//check if the pages can cater this.
					if(element.html().contains("Page$"+(pages+1)) ){
						goPageByPageWithCookie(cookies, pages, _viewState,_eventValidation);
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
		if(month.length() == 1){
			month = "0"+month;
		}
		
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
		
		if(args.length != 3){
			System.err.println("Please enter the [month], [year] and [file location + name:"
					+ "\n SSLExtractor [month] [year] [location]"
					+ "\n E.g. java -cp SSLExtractor.jar com.jaring.jom.SSLExtractor 01 2014 c:/tmp/test.html");
			System.exit(1);
		}
		
		checkIfMonthYearValid(args[0],args[1]);
		
		checkFolderExist(args[2]);
		
		new SSLExtractor(args[0], args[1], args[2]);
	}
}
