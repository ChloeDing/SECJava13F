
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
 * This one is used to convert SEC F13 XML files to excel (.xls) file with three
 * columes (consolidated company names, values and shares)
 * 
 * @author dliu
 *
 */

public class SECF13Converter {

	public static void main(String[] args) {

		// download the xml file from given url
		String xmlUrlString = "https://www.sec.gov/Archives/edgar/data/813917/000081391717000004/halp13F.xml";
		Document inputDoc = XMLUtil.getDocumentFromURL(xmlUrlString);
		// System.out.println("doc size: " + inputDoc.getNodeName());

		try {
			f13convert(inputDoc);
			System.out.println(
					"SEC F13 XML successfualy converted to excel (.xls) file with three columes (consolidated company names, values and shares).");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("SEC F13 XML failed to converted to excel (.xls) file.");
			return;
		}
	}

	private static void f13convert(Document inputDoc) throws Exception {
		// in
		Element root = inputDoc.getDocumentElement();

		// company list
		NodeList companyList = root.getElementsByTagName("infoTable");
		// System.out.println("companyList: " + companyList.getLength());

		// LinkedHashMap between the pair of company (the key) and value
		LinkedHashMap<String, Long> companyValuePair = new LinkedHashMap<String, Long>();

		// LinkedHashMap between the pair of company (the key) and shares
		LinkedHashMap<String, Long> companySharePair = new LinkedHashMap<String, Long>();

		// loop through the company list to construct the company-value pair and
		// the company-shares pair
		for (int i = 0; i < companyList.getLength(); i++) {
			String nameOfIssuer = ((Element) ((Element) companyList.item(i)).getElementsByTagName("nameOfIssuer")
					.item(0)).getTextContent();
			String value = ((Element) ((Element) companyList.item(i)).getElementsByTagName("value").item(0))
					.getTextContent();
			String shares = ((Element) ((Element) companyList.item(i)).getElementsByTagName("shrsOrPrnAmt").item(0))
					.getElementsByTagName("sshPrnamt").item(0).getTextContent();
			// System.out.println("nameOfIssuer: " + nameOfIssuer);
			// System.out.println("value: " + value);
			// System.out.println("shares: " + shares);

			if (companyValuePair.get(nameOfIssuer) == null) {
				companyValuePair.put(nameOfIssuer, Long.parseLong(value));
			} else {
				companyValuePair.put(nameOfIssuer, companyValuePair.get(nameOfIssuer) + Long.parseLong(value));
			}

			if (companySharePair.get(nameOfIssuer) == null) {
				companySharePair.put(nameOfIssuer, Long.parseLong(shares));
			} else {
				companySharePair.put(nameOfIssuer, companySharePair.get(nameOfIssuer) + Long.parseLong(shares));
			}
		}

		// out : write LinkedHashMap into excel file
		File outFile = new File("ExcelFolder\\f13.xls");
		XSSFWorkbook myWorkBook = new XSSFWorkbook();
		XSSFSheet mySheet = myWorkBook.createSheet();
		int rownum = mySheet.getLastRowNum();

		Row titleRow = mySheet.createRow(rownum++);
		Cell titleName = titleRow.createCell(0);
		titleName.setCellValue("Company");

		Cell titleValue = titleRow.createCell(1);
		titleValue.setCellValue("Value ($ thousands)");

		Cell titleCount = titleRow.createCell(2);
		titleCount.setCellValue("Shares");

		for (String name : companyValuePair.keySet()) {
			Row row = mySheet.createRow(rownum++);
			Cell cellNameOfIssuer = row.createCell(0);
			cellNameOfIssuer.setCellValue(name);

			Cell cellValue = row.createCell(1);
			cellValue.setCellValue((Long) companyValuePair.get(name));

			Cell cellCount = row.createCell(2);
			cellCount.setCellValue((Long) companySharePair.get(name));
		}

		FileOutputStream os = new FileOutputStream(outFile);
		myWorkBook.write(os);
		myWorkBook.close();
		os.close();
	}
}
