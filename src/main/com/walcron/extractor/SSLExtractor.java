package com.walcron.extractor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Pattern;

public class SSLExtractor {
	private final String FILE_EXT = ".csv";
	private final int MAX_PAGE=200; //create a buffer limit
	private final int TIME_OUT= 30000;

	public SSLExtractor(){
		try{

			Response con_query = Jsoup.connect("http://www.raffles1.net/currency.php")
					.timeout(TIME_OUT)
					.method(Method.GET)
					.execute();
			Document docConTemp = con_query.parse();
			Elements tableContent = docConTemp.getElementsByAttributeValueContaining("class", "main_link");
			buildDataFromRateTable(tableContent);
		}catch(Exception e){
			e.printStackTrace();
			System.err.println("Oops hit error in reading the HTML.Error:-"+e.getMessage());
		}
	}

	private void buildDataFromRateTable(Elements tableContent) {

		StringBuilder rateTable = new StringBuilder(50 * tableContent.size());
		String rateDate = null;

		for(Element element: tableContent) {
			Elements rowItems = element.getElementsByTag("td");
			if(rowItems.size() == 1) {
				rateDate = extractAsDate(rowItems.get(0).text());
			}
			else if(rowItems.size() > 1) {
				rateTable.append(extractRates(rowItems)).append('\n');
			}
		}

		if(rateDate != null) {
			storeToFile(rateDate, rateTable);
		}
		else {
			System.err.println("No rates found today");
		}
	}

	private String extractAsDate(String date) {
		//Reformat date to be nonspace and readable
		date = date.replaceFirst("([a-z|A-Z|\\W])+","");
		date = date.replaceAll(" ","_");
		date = date.replaceAll(":",".");
		return date;
	}

	private StringBuilder extractRates(Elements rateElements) {
		StringBuilder codeCurrUnitSellBuy = new StringBuilder(100);
		codeCurrUnitSellBuy.append(rateElements.get(0).text()).append(',');
		codeCurrUnitSellBuy.append(rateElements.get(1).text()).append(',');
		codeCurrUnitSellBuy.append(rateElements.get(2).text()).append(',');
		codeCurrUnitSellBuy.append(rateElements.get(3).text()).append(',');
		codeCurrUnitSellBuy.append(rateElements.get(4).text());
		return codeCurrUnitSellBuy;
	}

	private void storeToFile(String rateDate, StringBuilder rateTable){
		try{
			File file = new File(rateDate + this.FILE_EXT);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(rateTable.toString().getBytes());
			fos.close();
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
	}

	public static void main(String args[]){
		new SSLExtractor();
	}
}
