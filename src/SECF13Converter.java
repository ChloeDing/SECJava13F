
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;

import javax.swing.*;

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
 * @author dliu & tyang
 *
 */

public class SECF13Converter extends JFrame {

	private JLabel f13linkAJLable;
	private JLabel f13linkBJLable;
	private JTextField f13linkAJLableJTextField;
	private JTextField f13linkBJLableJTextField;
	private JButton covertAtoExcel;
	private JButton covertBtoExcel;
	private JButton compareJButton;
	private JTextArea resultMessageJTextArea;

	private LinkedHashMap<String, Double[]> companyValueShareHashMap;
	private LinkedHashMap<String, Double[]> companyValueShareBIGHashMap;

	public static void main(String[] args) {

		SECF13Converter application = new SECF13Converter();
		application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	private SECF13Converter() {

		createUserInterface();

	}

	private void createUserInterface() {

		Container contentPane = getContentPane();
		contentPane.setLayout(null);

		f13linkAJLable = new JLabel();
		f13linkAJLable.setBounds(40, 40, 120, 20);
		f13linkAJLable.setText("XML URL for F13 A:");
		contentPane.add(f13linkAJLable);

		f13linkAJLableJTextField = new JTextField();
		f13linkAJLableJTextField.setBounds(160, 40, 380, 20);
		contentPane.add(f13linkAJLableJTextField);

		covertAtoExcel = new JButton();
		covertAtoExcel.setBounds(550, 40, 125, 20);
		covertAtoExcel.setText("Covert to EXCEL");
		contentPane.add(covertAtoExcel);
		covertAtoExcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				covertAtoExcelActionPerformed(event);
			}
		});

		f13linkBJLable = new JLabel();
		f13linkBJLable.setBounds(40, 80, 120, 20);
		f13linkBJLable.setText("XML URL for F13 B:");
		contentPane.add(f13linkBJLable);

		f13linkBJLableJTextField = new JTextField();
		f13linkBJLableJTextField.setBounds(160, 80, 380, 20);
		contentPane.add(f13linkBJLableJTextField);

		covertBtoExcel = new JButton();
		covertBtoExcel.setBounds(550, 80, 125, 20);
		covertBtoExcel.setText("Covert to EXCEL");
		contentPane.add(covertBtoExcel);
		covertBtoExcel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				covertBtoExcelActionPerformed(event);
			}
		});

		compareJButton = new JButton();
		compareJButton.setBounds(240, 150, 200, 40);
		compareJButton.setText("Compare F13 A and F13 B");
		contentPane.add(compareJButton);
		compareJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				compareJButtonActionPerformed(event);
			}
		});

		resultMessageJTextArea = new JTextArea();
		resultMessageJTextArea.setBounds(40, 250, 620, 150);
		resultMessageJTextArea.setText("");
		resultMessageJTextArea.setEditable(false);
		resultMessageJTextArea.setLineWrap(true);
		contentPane.add(resultMessageJTextArea);

		setTitle("SEC F13 Compare"); // set interface title
		setSize(720, 500); // set window size
		setLocation(500, 200);
		setVisible(true); // display window
	}

	private void covertAtoExcelActionPerformed(ActionEvent event) {

		String xmlUrlStringA = f13linkAJLableJTextField.getText();
		// download the xml file from given url
		Document inputDocA = XMLUtil.getDocumentFromURL(xmlUrlStringA);
		// System.out.println("doc size: " + inputDocA.getNodeName());

		try {
			f13ConvertToHashMap(inputDocA);
			f13HashMaptoFile("A");
			resultMessageJTextArea.append("\n" + "SEC F13 A successfualy converted to excel.");
		} catch (Exception e) {
			e.printStackTrace();
			resultMessageJTextArea.append("\n" + "Failed to covert SEC F13 A to excel.");
			return;
		}

	}

	private void covertBtoExcelActionPerformed(ActionEvent event) {

		String xmlUrlStringB = f13linkBJLableJTextField.getText();
		// download the xml file from given url
		Document inputDocB = XMLUtil.getDocumentFromURL(xmlUrlStringB);
		// System.out.println("doc size: " + inputDocB.getNodeName());

		try {
			f13ConvertToHashMap(inputDocB);
			f13HashMaptoFile("B");
			resultMessageJTextArea.append("\n" + "SEC F13 B successfualy converted to excel.");
		} catch (Exception e) {
			e.printStackTrace();
			resultMessageJTextArea.append("\n" + "Failed to covert SEC F13 B to excel.");
			return;
		}

	}

	private void compareJButtonActionPerformed(ActionEvent event) {

		String xmlUrlStringA = f13linkAJLableJTextField.getText();
		// download the xml file from given url
		Document inputDocA = XMLUtil.getDocumentFromURL(xmlUrlStringA);
		// System.out.println("doc size: " + inputDocA.getNodeName());

		String xmlUrlStringB = f13linkBJLableJTextField.getText();
		// download the xml file from given url
		Document inputDocB = XMLUtil.getDocumentFromURL(xmlUrlStringB);
		// System.out.println("doc size: " + inputDocB.getNodeName());

		try {
			bothf13ConvertToBIGHashMap(inputDocA, inputDocB);
			f13BIGHashMaptoFile("CompareResult");
			resultMessageJTextArea.append("\n" + "Compare successfully completed and saved to excel.");
		} catch (Exception e) {
			e.printStackTrace();
			resultMessageJTextArea.append("\n" + "Failed to compare F13 A and F13 B.");
			return;
		}

	}

	private void f13ConvertToHashMap(Document inputDoc) {
		// in
		Element root = inputDoc.getDocumentElement();

		// company list
		NodeList companyList = root.getElementsByTagName("infoTable");
		// System.out.println("companyList: " + companyList.getLength());

		companyValueShareHashMap = new LinkedHashMap<String, Double[]>();

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

			Double[] value_shares = { Double.parseDouble(value), Double.parseDouble(shares) };
			// value_shares[0] is value and value_shares[1] is share

			if (companyValueShareHashMap.get(nameOfIssuer) == null) {
				companyValueShareHashMap.put(nameOfIssuer, value_shares);
			} else {
				Double[] new_values_shares = { companyValueShareHashMap.get(nameOfIssuer)[0] + value_shares[0],
						companyValueShareHashMap.get(nameOfIssuer)[1] + value_shares[1] };
				companyValueShareHashMap.put(nameOfIssuer, new_values_shares);
			}
		}

	}

	private void bothf13ConvertToBIGHashMap(Document A, Document B) {

		f13ConvertToHashMap(A);
		LinkedHashMap<String, Double[]> HMA = companyValueShareHashMap;
		f13ConvertToHashMap(B);
		LinkedHashMap<String, Double[]> HMB = companyValueShareHashMap;

		companyValueShareBIGHashMap = new LinkedHashMap<String, Double[]>();

		for (String name : HMA.keySet()) {
			if (HMB.get(name) == null) {
				HMB.put(name, new Double[] { (double) 0, (double) 0 });
			}
		}

		for (String name : HMB.keySet()) {
			if (HMA.get(name) == null) {
				HMA.put(name, new Double[] { (double) 0, (double) 0 });
			}
		}

		// Now HMA and HMB has the same total keys, so no difference between
		// HMA.KeySet() or HMB.KeySet()
		for (String name : HMA.keySet()) {
			Double[] big_value_shares = { HMA.get(name)[0], HMA.get(name)[1], HMB.get(name)[0], HMB.get(name)[1] };
			companyValueShareBIGHashMap.put(name, big_value_shares);
		}

	}

	private void f13HashMaptoFile(String filename) throws Exception {
		// out : write LinkedHashMap into excel file
		File outFile = new File("ExcelFolder\\f13" + filename + ".xls");
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

		for (String name : companyValueShareHashMap.keySet()) {
			Row row = mySheet.createRow(rownum++);
			Cell cellNameOfIssuer = row.createCell(0);
			cellNameOfIssuer.setCellValue(name);

			Cell cellValue = row.createCell(1);
			cellValue.setCellValue((Double) companyValueShareHashMap.get(name)[0]); // value

			Cell cellCount = row.createCell(2);
			cellCount.setCellValue((Double) companyValueShareHashMap.get(name)[1]); // share
		}

		FileOutputStream os = new FileOutputStream(outFile);
		myWorkBook.write(os);
		myWorkBook.close();
		os.close();
	}

	private void f13BIGHashMaptoFile(String filename) throws Exception {
		// out : write LinkedHashMap into excel file
		File outFile = new File("ExcelFolder\\f13" + filename + ".xls");
		XSSFWorkbook myWorkBook = new XSSFWorkbook();
		XSSFSheet mySheet = myWorkBook.createSheet();
		int rownum = mySheet.getLastRowNum();

		Row titleRow = mySheet.createRow(rownum++);
		Cell titleName = titleRow.createCell(0);
		titleName.setCellValue("Company");

		Cell titleValueA = titleRow.createCell(1);
		titleValueA.setCellValue("Value ($ thousands) of A");

		Cell titleCountA = titleRow.createCell(2);
		titleCountA.setCellValue("Shares of A");
		
		Cell titleValueB = titleRow.createCell(3);
		titleValueB.setCellValue("Value ($ thousands) of B");

		Cell titleCountB = titleRow.createCell(4);
		titleCountB.setCellValue("Shares of B");

		for (String name : companyValueShareBIGHashMap.keySet()) {
			Row row = mySheet.createRow(rownum++);
			Cell cellNameOfIssuer = row.createCell(0);
			cellNameOfIssuer.setCellValue(name); // name

			Cell cellValueA = row.createCell(1);
			cellValueA.setCellValue((Double) companyValueShareBIGHashMap.get(name)[0]); // value A

			Cell cellCountA = row.createCell(2);
			cellCountA.setCellValue((Double) companyValueShareBIGHashMap.get(name)[1]); // share A
			
			Cell cellValueB = row.createCell(3);
			cellValueB.setCellValue((Double) companyValueShareBIGHashMap.get(name)[2]); // value B

			Cell cellCountB = row.createCell(4);
			cellCountB.setCellValue((Double) companyValueShareBIGHashMap.get(name)[3]); // share B
		}

		FileOutputStream os = new FileOutputStream(outFile);
		myWorkBook.write(os);
		myWorkBook.close();
		os.close();

	}

}
