
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This one is used to convert XML files from the old format to the new format
 * once and for all
 * 
 * @author dliu
 *
 */
public class Converter {

	public static void main(String[] args) {
		// download the xml file from given url
		String urlString = "https://www.sec.gov/Archives/edgar/data/813917/000081391717000004/halp13F.xml";
		Document inputDoc = XMLUtil.getDocumentFromURL(urlString);
		// System.out.println("doc size: " + inputDoc.getNodeName());
		try {
			convert(inputDoc);
			System.out.println("success!");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("failed!");
			return;
		}
	}

	private static void convert(Document inputDoc) throws Exception {
		// in
		Element root = inputDoc.getDocumentElement();
		NodeList infoTableList = root.getElementsByTagName("infoTable");
		// System.out.println("infoTableList: " + infoTableList.getLength());
		LinkedHashMap<String, Long> nameValuePair = new LinkedHashMap<String, Long>(); // nameOfIssuer
																						// -
																						// value
		LinkedHashMap<String, Long> nameCountPair = new LinkedHashMap<String, Long>(); // nameOfIssuer
																						// -
																						// sshPrnamt
		// loop through the nodeList to construct the key-value pair
		for (int i = 0; i < infoTableList.getLength(); i++) {
			String nameOfIssuer = ((Element) ((Element) infoTableList.item(i)).getElementsByTagName("nameOfIssuer")
					.item(0)).getTextContent();
			String value = ((Element) ((Element) infoTableList.item(i)).getElementsByTagName("value").item(0))
					.getTextContent();
			String amount = ((Element) ((Element) infoTableList.item(i)).getElementsByTagName("shrsOrPrnAmt").item(0))
					.getElementsByTagName("sshPrnamt").item(0).getTextContent();
			// System.out.println("nameOfIssuer: " + nameOfIssuer);
			// System.out.println("value: " + value);
			// System.out.println("amount: " + amount);
			if (nameValuePair.get(nameOfIssuer) == null) {
				nameValuePair.put(nameOfIssuer, Long.parseLong(value));
			} else {
				nameValuePair.put(nameOfIssuer, nameValuePair.get(nameOfIssuer) + Long.parseLong(value));
			}

			if (nameCountPair.get(nameOfIssuer) == null) {
				nameCountPair.put(nameOfIssuer, Long.parseLong(amount));
			} else {
				nameCountPair.put(nameOfIssuer, nameCountPair.get(nameOfIssuer) + Long.parseLong(amount));
			}
		}

		// out : write LinkedHashMap into excel file
		File outFile = new File("output.xls");
		XSSFWorkbook myWorkBook = new XSSFWorkbook ();
		XSSFSheet mySheet =  myWorkBook.createSheet();
		int rownum = mySheet.getLastRowNum();
		
		Row titleRow = mySheet.createRow(rownum++);
		Cell titleName = titleRow.createCell(0);
		titleName.setCellValue("Company");
		
		Cell titleValue = titleRow.createCell(1);
		titleValue.setCellValue("Value (x$1,000)");
		
		Cell titleCount = titleRow.createCell(2);
		titleCount.setCellValue("Shares");
		
		for (String name : nameValuePair.keySet()) {
			Row row = mySheet.createRow(rownum ++);
			Cell cellNameOfIssuer = row.createCell(0);
			cellNameOfIssuer.setCellValue(name);
			
			Cell cellValue = row.createCell(1);
			cellValue.setCellValue((Long)nameValuePair.get(name));
			
			Cell cellCount = row.createCell(2);
			cellCount.setCellValue((Long)nameCountPair.get(name));
		}
		
		FileOutputStream os = new FileOutputStream(outFile);
		myWorkBook.write(os);
		myWorkBook.close();
		os.close();
	}
}
