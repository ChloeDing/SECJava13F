
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
	private JButton convertXMLtoEXCELJButton;
	private JTextArea resultMessageJTextArea;

	private LinkedHashMap<String, Double> companyValuePair;
	private LinkedHashMap<String, Double> companySharePair;

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
		f13linkAJLable.setBounds(40, 40, 90, 20);
		f13linkAJLable.setText("URL for XML A:");
		contentPane.add(f13linkAJLable);

		f13linkAJLableJTextField = new JTextField();
		f13linkAJLableJTextField.setBounds(150, 40, 300, 20);
		contentPane.add(f13linkAJLableJTextField);

		f13linkBJLable = new JLabel();
		f13linkBJLable.setBounds(40, 80, 90, 20);
		f13linkBJLable.setText("URL for XML B:");
		contentPane.add(f13linkBJLable);

		f13linkBJLableJTextField = new JTextField();
		f13linkBJLableJTextField.setBounds(150, 80, 300, 20);
		contentPane.add(f13linkBJLableJTextField);

		convertXMLtoEXCELJButton = new JButton();
		convertXMLtoEXCELJButton.setBounds(150, 150, 200, 40);
		convertXMLtoEXCELJButton.setText("Convert XML to EXCEL");
		contentPane.add(convertXMLtoEXCELJButton);
		convertXMLtoEXCELJButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				convertXMLtoEXCELJButtonActionPerformed(event);
			}

		});

		resultMessageJTextArea = new JTextArea();
		resultMessageJTextArea.setBounds(80, 250, 350, 150);
		resultMessageJTextArea.setText("");
		resultMessageJTextArea.setEditable(false);
		contentPane.add(resultMessageJTextArea);

		setTitle("XML to EXCEL"); // set interface title
		setSize(600, 600); // set window size
		setVisible(true); // display window
	}

	private void convertXMLtoEXCELJButtonActionPerformed(ActionEvent event) {

		String xmlUrlStringA = f13linkAJLableJTextField.getText();
		// download the xml file from given url
		Document inputDocA = XMLUtil.getDocumentFromURL(xmlUrlStringA);
		// System.out.println("doc size: " + inputDocA.getNodeName());

		try {
			f13ConvertToHashMap(inputDocA);
			f13HashMaptoFile("A");
			resultMessageJTextArea.append(
					"SEC F13 XML A successfualy converted to excel (.xls) file with three columes (consolidated company names, values and shares).");
		} catch (Exception e) {
			e.printStackTrace();
			resultMessageJTextArea.append("SEC F13 XML A failed to converted to excel file.");
			return;
		}

		String xmlUrlStringB = f13linkBJLableJTextField.getText();
		// download the xml file from given url
		Document inputDocB = XMLUtil.getDocumentFromURL(xmlUrlStringB);
		// System.out.println("doc size: " + inputDocB.getNodeName());

		try {
			f13ConvertToHashMap(inputDocB);
			f13HashMaptoFile("B");
			resultMessageJTextArea.append(
					"\n" + "SEC F13 XML B successfualy converted to excel (.xls) file with three columes (consolidated company names, values and shares).");
		} catch (Exception e) {
			e.printStackTrace();
			resultMessageJTextArea.append("\n" + "SEC F13 XML B failed to converted to excel file.");
			return;
		}

	}

	private void f13ConvertToHashMap(Document inputDoc) {
		// in
		Element root = inputDoc.getDocumentElement();

		// company list
		NodeList companyList = root.getElementsByTagName("infoTable");
		// System.out.println("companyList: " + companyList.getLength());

		// LinkedHashMap between the pair of company (the key) and value
		companyValuePair = new LinkedHashMap<String, Double>();

		// LinkedHashMap between the pair of company (the key) and shares
		companySharePair = new LinkedHashMap<String, Double>();

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
				companyValuePair.put(nameOfIssuer, Double.parseDouble(value));
			} else {
				companyValuePair.put(nameOfIssuer, companyValuePair.get(nameOfIssuer) + Long.parseLong(value));
			}

			if (companySharePair.get(nameOfIssuer) == null) {
				companySharePair.put(nameOfIssuer, Double.parseDouble(shares));
			} else {
				companySharePair.put(nameOfIssuer, companySharePair.get(nameOfIssuer) + Long.parseLong(shares));
			}
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

		for (String name : companyValuePair.keySet()) {
			Row row = mySheet.createRow(rownum++);
			Cell cellNameOfIssuer = row.createCell(0);
			cellNameOfIssuer.setCellValue(name);

			Cell cellValue = row.createCell(1);
			cellValue.setCellValue((Double) companyValuePair.get(name));

			Cell cellCount = row.createCell(2);
			cellCount.setCellValue((Double) companySharePair.get(name));
		}

		FileOutputStream os = new FileOutputStream(outFile);
		myWorkBook.write(os);
		myWorkBook.close();
		os.close();
	}
}
