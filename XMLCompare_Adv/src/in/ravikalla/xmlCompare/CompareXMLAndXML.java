package in.ravikalla.xmlCompare;
/*
 * Copyright (c) 1995, 2008, Ravi Kalla. All rights reserved.
 * Author : ravi2523096@gmail.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Ravi Kalla or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import in.ravikalla.xmlCompare.dto.XMLToXMLComparisonResultsHolderDTO;
import in.ravikalla.xmlCompare.util.CommonUtil;
import in.ravikalla.xmlCompare.util.ConvertXMLToFullPathInCSV;
import in.ravikalla.xmlCompare.util.XMLDataConverter;

/**
 * 
 * 
 * Compare two XMLs that has elements in random order and write differences in and Excel file.
 * 
 * @author ravi2523096@gmail.com
 * @since 31-May-2016
 * 
 * Current Features:
 * =================
 * 1. Compare XMLs with elements in any order
 * 2. Tested on 6MB XML files.
 * 3. Ignore elements while comparing
 * 4. Trim elements while comparing
 * 5. Auto identification of first level of repeating elements
 * 
 * TODO - New Features:
 * ================
 * 1. Consider prefix for elements
 * 2. Consider attributes
 * 3. Consider a primary key for repeating elements
 *
 */
public class CompareXMLAndXML {
	public static void main(String[] args) {
		System.out.println("Start : CompareXMLAndXML.main(...)");
		String strXMLFileName1 = "resources/XML1.xml";
		String strXMLFileName2 = "resources/XML2.xml";
		String strExcludeElementsFileName = null;
		String strIterateElementsFileName = null;
		String strComparisonResultsFile = "resources/Results.xls";
		String strTrimElements = null;
		try {
			testCompareXMLAndXML_WriteResults(strXMLFileName1, strXMLFileName2, strExcludeElementsFileName,
					strIterateElementsFileName, strComparisonResultsFile, strTrimElements);
		} catch (IOException e) {
			System.out.println("31 : CompareXMLAndXML.main(...) : IOException e : " + e);
		}
		System.out.println("End : CompareXMLAndXML.main(...)");
	}

	public static boolean testCompareXMLAndXML_WriteResults(String strXMLFileName1, String strXMLFileName2,
			String strExcludeElementsFileName, String strIterateElementsFileName, String strComparisonResultsFile,
			String strTrimElements) throws IOException {
		System.out.println("Start : CompareXMLAndXML.testCompareXMLAndXML_WriteResults()" + strXMLFileName1 + " : " + strXMLFileName2);
		String xmlStr1 = CommonUtil.readDataFromFile(strXMLFileName1);
		String xmlStr2 = CommonUtil.readDataFromFile(strXMLFileName2);
		List<String> lstElementsToExclude = readTxtFileToList(strExcludeElementsFileName);
		List<String> lstIterativeElements = null;
		String strPrimaryNodeXMLElementName = null;
		if (null != strIterateElementsFileName)
			lstIterativeElements = readTxtFileToList(strIterateElementsFileName);
		else
			lstIterativeElements = ConvertXMLToFullPathInCSV.getFirstLevelOfReapeatingElements(xmlStr1, xmlStr2);

		boolean testResult = CompareXMLAndXML.compareXMLAndXML_WriteResults(
				strComparisonResultsFile, xmlStr1, xmlStr2,
				lstIterativeElements, lstElementsToExclude,
				strPrimaryNodeXMLElementName, strTrimElements
				);
		System.out.println("End : CompareXMLAndXML.testCompareXMLAndXML_WriteResults()" + strXMLFileName1 + " : " + strXMLFileName2 + " : " + testResult);
		return testResult;
	}
	public static boolean compareXMLAndXML_WriteResults(String strComparisonResultsFile, String xmlStr1,
			String xmlStr2, List<String> lstIterativeElements, List<String> lstElementsToExclude,
			String strPrimaryNodeXMLElementName, String strTrimElements) {
		xmlStr1 = replaceEscapes(xmlStr1);
		xmlStr2 = replaceEscapes(xmlStr2);

		printParametersOfXMLtoXMLComparison(strComparisonResultsFile, xmlStr1,
				xmlStr2, lstIterativeElements, lstElementsToExclude,
				strPrimaryNodeXMLElementName, strTrimElements);

		boolean blnDifferencesExists = false;

		try {
			System.out.println("Iterative elements length : " + lstIterativeElements.size());
			XMLToXMLComparisonResultsHolderDTO objXMLToXMLComparisonResultsHolderDTO = null;
			List<String> lstMatchedDataForCSV = new ArrayList<String>();
			List<String> lstMismatchedDataForCSV = new ArrayList<String>();
			lstMatchedDataForCSV.add("Expected XPath,Expected Data,Actual Data");
			lstMismatchedDataForCSV.add("Expected XPath,Expected Data,Actual Path,Actual Data");

			for (int intCtr = 0; intCtr < lstIterativeElements.size(); intCtr++) {
				String strIterationElement = lstIterativeElements.get(intCtr);
				if (!strIterationElement.equals("")) {
					objXMLToXMLComparisonResultsHolderDTO = XMLDataConverter
							.compareXPathElementsData_WithChildElements(
									xmlStr1, xmlStr2,
									lstIterativeElements.get(intCtr),
									lstElementsToExclude,
									strPrimaryNodeXMLElementName,
									strTrimElements);
					lstMatchedDataForCSV.addAll(objXMLToXMLComparisonResultsHolderDTO.lstMatchedDataForCSV);
					lstMismatchedDataForCSV.addAll(objXMLToXMLComparisonResultsHolderDTO.lstMismatchedDataForCSV);
				}
			}
			System.out.println("Mismatched data size : " + lstMismatchedDataForCSV.size());
			if (lstMismatchedDataForCSV.size() > 1)
				blnDifferencesExists = true;
			XMLDataConverter.printResultsToFile(strComparisonResultsFile, lstMatchedDataForCSV, lstMismatchedDataForCSV);
		} catch (SAXException e) {
			System.out.println("79 : CompareXMLAndXML.compareXMLAndXML_WriteResults(...) : SAXException e : " + e);
		} catch (IOException e) {
			System.out.println("81 : CompareXMLAndXML.compareXMLAndXML_WriteResults(...) : IOException e : " + e);
		} catch (ParserConfigurationException e) {
			System.out.println("83 : CompareXMLAndXML.compareXMLAndXML_WriteResults(...) : ParserConfigurationException e : " + e);
		}
		return !blnDifferencesExists;
	}
	private static void printParametersOfXMLtoXMLComparison(String strComparisonResultsFile, String xmlStr1,
			String xmlStr2, List<String> lstIterativeElements, List<String> lstElementsToExclude,
			String strPrimaryNodeXMLElementName, String strTrimElements) {
		PrintWriter out = null;
		try {
			String strIterativeElements = lstIterativeElements.stream().map(Object::toString).collect(Collectors.joining(","));
			String strElementsToExclude = lstElementsToExclude.stream().map(Object::toString).collect(Collectors.joining(","));
			out = new PrintWriter(strComparisonResultsFile + "_Params");
			out.println(strComparisonResultsFile + "\n@xmlStr1 : " + xmlStr1 + "\n@xmlStr2 : "
					+ xmlStr2 + "\n@strIterativeElement : " + strIterativeElements
					+ "@lstElementsToExclude : " + strElementsToExclude
					+ "@strPrimaryNodeXMLElementName : " + strPrimaryNodeXMLElementName
					+ "@strTrimElements : " + strTrimElements
					);
		} catch (FileNotFoundException e) {
			System.out.println("103 : CompareXMLAndXML.printParametersOfXMLtoXMLComparison(...) : FileNotFoundException e : " + e);
		} finally {
			if (null != out)
				out.close();
		}
	}
	private static String replaceEscapes(String xmlStr) {
		xmlStr = xmlStr.replaceAll("&lt;", "<").replaceAll("<\\?.*?\\?>", "");
		xmlStr = xmlStr.replaceAll("&gt;", ">").replaceAll("<\\?.*?\\?>", "");
		return xmlStr;
	}
	private static List<String> readTxtFileToList(String strFileName) {
		System.out.println("Start : CompareXMLAndXML.readTxtFileToList(...)");
		BufferedReader br = null;
		String strLine = null;
		List<String> lstLines = new ArrayList<String>();
		try {
			if (null != strFileName && strFileName.trim().length() > 0) {
				br = new BufferedReader(new BufferedReader(new FileReader(strFileName)));
				while ((strLine = br.readLine()) != null)
					lstLines.add(strLine);
			}
		} catch (FileNotFoundException e) {
			System.out.println("39 : CompareXMLAndXML.readTxtFileToList(...) : FileNotFoundException e : " + e);
		} catch (IOException e) {
			System.out.println("41 : CompareXMLAndXML.readTxtFileToList(...) : IOException e : " + e);
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("47 : CompareXMLAndXML.readTxtFileToList(...) : IOException e : " + e);
				}
			}
		}
		System.out.println("End : CompareXMLAndXML.readTxtFileToList(...)");
		return lstLines;
	}
}
