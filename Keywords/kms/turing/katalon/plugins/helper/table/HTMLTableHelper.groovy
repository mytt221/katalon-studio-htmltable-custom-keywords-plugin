package kms.turing.katalon.plugins.helper.table

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.model.FailureHandling

import kms.turing.katalon.plugins.common.WebElementExt
import kms.turing.katalon.plugins.helper.WebElementHelper
import kms.turing.katalon.plugins.helper.WebPageHelper
import kms.turing.katalon.plugins.helper.XPathHelper
import kms.turing.katalon.plugins.helper.XPathHelper.CompareOptions
import kms.turing.katalon.plugins.helper.table.WebTableHelper.CellTextOptions
import kms.turing.katalon.plugins.utils.ListUtils

public class HTMLTableHelper extends WebTableHelper{

	static final String FIND_TABLE_BY_ROW_XPATH = "//table[*[tr[@{rowCriteria}]]]"
	static final String FIND_CELLS_BY_COLUMN_INDEX = "//tr[.//td[ position()= 1]|.//th[position()=0]]/*[(local-name()='td' or local-name()='th') and count(./preceding-sibling::*[local-name()='td' or local-name()='th'])=@{columnIndex}]"

	static Map<Integer, String>cachedHeaders = [:]
	/**
	 * identify a web table based on provided column headers
	 * @param columnHeaders the list of column headers used for detecting the table
	 * @param timeout time for searching the web table in seconds
	 * @return WebElement
	 */
	@Keyword
	public static WebElement identifyTableByColumnHeaders(List<String> columnHeaders, int timeout = 0, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "identify web table using headers: $columnHeaders"
		try{
			String xpath = "";

			List<String> headerTextXpaths = []
			columnHeaders.each{ header ->
				def thTextXpath = XPathHelper.makeTextComparisionXPath('.', header, CompareOptions.CONTAINS)
				thTextXpath = "th[$thTextXpath]"
				headerTextXpaths.add(thTextXpath)
			}

			String rowCriteria = headerTextXpaths.join(' and ')
			xpath = FIND_TABLE_BY_ROW_XPATH.replace("@{rowCriteria}", rowCriteria)

			def usingTdXpath = xpath.replaceAll("th\\[", "td\\[")
			xpath="$xpath|$usingTdXpath"
			WebElement table = WebElementHelper.findWebElement(null, By.xpath(xpath), timeout)
			backupElement(table, null, By.xpath(xpath))
			cachedHeaders = getHeaderIndexes(table)
			return table
		}
		catch(ex){
			handleError ex, flowControl
			return null
		}
	}

	/**
	 * Verify a row displaying in a table using it's cells info
	 * @param table the WebElement table used for verifying if a row displayed
	 * @param cellsInfo a map of cells info including column's header and cell value
	 * @return true/false 
	 */
	@Keyword
	public static boolean verifyRowDisplayed(WebElement table, Map<String, Object> cellsInfo,  FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Verify a row containing the following cells $cellsInfo is displayed in the table $table"
		try{
			table = realocateIfStale(table)
			List<Map<String, Object>> cells = []
			cellsInfo.each{header, text ->
				cells.add([(COLUMN_HEADER): header, (VALUE): text, (CELL_TEXT_OPTION): CellTextOptions.CONTENT_TEXT, (COMPARE_OPTION): CompareOptions.EQUALS])
			}

			WebElement row = getMatchedRow(table, cells)
			handleErrorIf(!row, "Could not find any rows match to searching criteria ${cellsInfo}", flowControl)
			return row != null
		}catch(ex){
			handleError(ex, flowControl)
			return false
		}
	}

	/**
	 * Identity a cell web element using column header and cell value
	 * @param table the WebElement table which the cell belonging to
	 * @param columnHeader the column header that the cell display under
	 * @param cellValue the value displays on the cell
	 * @return WebElement
	 */
	@Keyword
	public static WebElement identifyCellByValueAndColHeader(WebElement table, String columnHeader, String cellValue, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "identity a cell with value '$cellValue' under column header '$columnHeader'"
		try{
			table = realocateIfStale(table)
			int columnIndex = getColumnIndex(table, '.', columnHeader)
			if(columnIndex > 0){
				String cellCriteria = XPathHelper.makeTextComparisionXPath('.', cellValue)
				String cellXpath = ".//tr/td[$cellCriteria and position() = $columnIndex]|.//tr/th[$cellCriteria and position() = $columnIndex]"
				WebElement cell = WebElementHelper.findWebElement(table, By.xpath(cellXpath), FIND_ELEMENT_TIMEOUT)
				handleErrorIf(cell == null, "Could not find any cell with text '$cellValue' under column header '$columnHeader'", flowControl)
				backupElement(cell, table, By.xpath(cellXpath))
				return cell
			}
			else{
				handleError("Could not find any columns with header '$columnHeader'", flowControl)
				return null
			}
		}catch(ex){
			handleError(ex, flowControl)
			return null
		}
	}

	/**
	 * Get column index by it's header
	 * @param table the WebElement table which the row belonging to
	 * @param columnHeader the column header
	 * @return integer, starts from 1
	 */
	@Keyword
	public static int getColumnIndexByHeader(WebElement table, String columnHeader, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Get index of column '$columnHeader'"
		try{
			table = realocateIfStale(table)
			int index = getColumnIndex(table, '.', columnHeader)
			if(index < 0){
				index = getColumnIndex(table, '.', columnHeader, CompareOptions.CONTAINS)
			}
			handleErrorIf(index <= 0, "Could not find column with header '$columnHeader'", flowControl)
			return index
		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	/**
	 * Get column index by it's attribute. It's used in case of missing column header
	 * @param table the WebElement table which the row belonging to
	 * @param attribute the attribute name used for detect position of the column
	 * @param value the attribute value used for detect position of the column
	 * @return integer, starts from 1
	 */
	@Keyword
	public static int getColumnIndexByAttribute(WebElement table, String attribute, String value, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Get index of column using it attribute $attribute = '$value'"
		try{
			table = realocateIfStale(table)
			int index = getColumnIndex(table, attribute, value)
			handleErrorIf(index < 0, "Could not find column with attribute $attribute = '$value'", flowControl)
			return index
		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	/**
	 * Get position of a column based on it's attribute
	 */
	private static int getColumnIndex(WebElement table, String attribute, String value){
		int index
		if(attribute != '.' && attribute != 'text()'){
			attribute = "@$attribute"
		}
		List<WebElement> columns = findColumnHeaders(table, attribute, value)
		if(!columns){
			Map<String, Integer> headerIndexes = getHeaderIndexes(table)
			int cahedIndex = getCachedHeaderIndex(value)
			def newHeader = headerIndexes.getAt(cahedIndex)
			if(newHeader && newHeader.toLowerCase().contains(value.toLowerCase())){
				columns = findColumnHeaders(table, attribute, newHeader)
			}
		}

		return columns ? columns.size() : - 1
	}

	private static List<WebElement> findColumnHeaders(WebElement table, String attribute, String value){
		def textXpath = XPathHelper.makeTextComparisionXPath(attribute, value)
		def thTextXpath = "th[$textXpath]"
		def tdTextXpath = "td[$textXpath]"

		def columsXpath = ".//tr/$thTextXpath|//tr/$tdTextXpath|.//tr/$thTextXpath/preceding-sibling::th|//tr/$tdTextXpath/preceding-sibling::td"
		info "Find columns matching to search criteira xpath: ${columsXpath}"
		List<WebElement> columns = WebElementHelper.findWebElements(table, By.xpath(columsXpath), FIND_ELEMENT_TIMEOUT)
		return columns
	}

	/**
	 * Get row index using it's cells info
	 * @param table the WebElement table which the row belonging to
	 * @param cellsInfo map of cells info including their column headers and cell values
	 * @return integer, starts from 1
	 */
	@Keyword
	public static int getRowIndexByCellsInfo(WebElement table, Map<String, Object> cellsInfo, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Get index of the row containing the following cells $cellsInfo"
		try{
			table = realocateIfStale(table)
			List<Map<String, Object>> cells = []
			cellsInfo.each{header, text ->
				cells.add([(COLUMN_HEADER): header, (VALUE): text, (CELL_TEXT_OPTION): CellTextOptions.CONTENT_TEXT, (COMPARE_OPTION): CompareOptions.EQUALS])
			}

			List<WebElement> rows = getMatchedAndPrecedingRows(table, cells)
			handleErrorIf(!rows, "Could not find any rows match to searching criteria ${cellsInfo}", flowControl)

			return isHeaderSeparated(table) ? rows.size(): rows.size() - 1
		}catch(ex){
			handleError(ex, flowControl)
			return -1
		}
	}

	/**
	 * Identify a web element cell of the specific table based on column header and another cells info 
	 * @param table the table passed in to identify the cell
	 * @param columnHeader the column header which that cell belonging to
	 * @param cellsInfo map of another cells info including their column headers and cell values to identify the row that cell belongings to
	 * @return WebElement
	 */
	@Keyword
	public static WebElement identifyCellByHeaderAndCellsInfo(WebElement table, String columnHeader, Map<String, Object> cellsInfo, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		try{
			table = realocateIfStale(table)
			info "identify a cell under column header '$columnHeader' belonging to the row of another cells $cellsInfo"
			int columnIndex = getColumnIndex(table, '.', columnHeader)

			if(columnIndex < 0){
				handleError("Could not find any columns with header '$columnHeader', flowControl")
				return null
			}

			//find row by provided cells info
			List<Map<String, Object>> cells = []
			cellsInfo.each{header, text ->
				cells.add([(COLUMN_HEADER): header, (VALUE): text, (CELL_TEXT_OPTION): CellTextOptions.CONTENT_TEXT, (COMPARE_OPTION): CompareOptions.EQUALS])
			}
			WebElement row = getMatchedRow(table, cells)
			handleErrorIf(!row, "Could not find any rows match to searching criteria ${cellsInfo}", flowControl)

			String cellXpath = ".//*[position()=$columnIndex and (local-name()='td' or local-name()='th')]"
			WebElement cell = WebElementHelper.findWebElement(row, By.xpath(cellXpath), FIND_ELEMENT_TIMEOUT)
			handleErrorIf(!cell, "Could not find any cell under column header '$columnHeader' belonging to the row of another cells $cellsInfo ", flowControl)
			backupElement(cell, table, By.xpath(cellXpath))
			return cell

		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	/**
	 * Identity a cell web element using row and column indexes
	 * @param table the WebElement table which the row belonging to
	 * @param columnIndex position of the column in the table
	 * @param rowIndex position of the row in the table
	 * @return WebElement
	 */
	@Keyword
	public static WebElement identifyCellByIndexes(WebElement table, int columnIndex, int rowIndex, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Identify a cell in table with row index:$rowIndex, column index: $columnIndex"

		try{
			table = realocateIfStale(table)
			def rows = WebElementHelper.findWebElements(table, By.tagName('tr'), FIND_ELEMENT_TIMEOUT)
			def rowNumber = rows.size()

			if(rowIndex <= 0 || rowIndex > rowNumber){
				handleError("Invalid row index: $rowIndex. It should start from 1", flowControl)
				return null
			}

			def row = rows[rowIndex]
			def cols = row.findElements(By.xpath(".//td|.//th"))

			// Scan all of columns to see if any columns use colspan attribute
			def originalValue = columnIndex
			def spanSum = 0
			def currentIndex = 0
			for (def col in cols){
				currentIndex++
				def colspan = col.getAttribute("colspan")
				if (colspan != null){
					def increasement = colspan.toInteger() - 1
					spanSum += increasement
					// If any colspan used, the colspan value subtracted from the index
					columnIndex = columnIndex - increasement
				}
				if (currentIndex + spanSum >= originalValue){
					break
				}
			}
			WebElement cell = cols[columnIndex-1]
			backupElement(cell, table, By.xpath(XPathHelper.GetElementXPath(cell)))
			return cell
		}catch(ex){
			handleError(ex, flowControl)
		}
	}


	/**
	 * Click on a column based on it' header
	 * @param table the table that the column belongings to
	 * @param columnHeader the column header 
	 * @return
	 */
	@Keyword
	public static void clickOnColumn(WebElement table, String columnHeader, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Click on column with header: '$columnHeader'"
		try{
			table = realocateIfStale(table)
			WebElement column = findColumn(table, columnHeader)
			if(!column){
				int cahedIndex = getCachedHeaderIndex(columnHeader)
				Map<String, Integer> headerIndexes = getHeaderIndexes(table)
				def newHeader = headerIndexes.getAt(cahedIndex)
				if(newHeader && newHeader.toLowerCase().contains(columnHeader.toLowerCase())){
					column = findColumn(table, newHeader)
				}
			}
			handleErrorIf(!column, "Could not find any columns with header '$columnHeader'", null)
			column.click()
			WebPageHelper.waitForPageLoad(PAGE_LOAD_TIMEOUT)
		}catch(ex){
			handleError ex, flowControl
		}
	}

	private static WebElement findColumn(WebElement table, String columnHeader){
		String textXpath = XPathHelper.makeTextComparisionXPath('.', columnHeader)
		String columnXpath = ".//tr/td[$textXpath]|.//tr/th[$textXpath]"
		return WebElementHelper.findWebElement(table, By.xpath(columnXpath), FIND_ELEMENT_TIMEOUT)
	}

	/**
	 * Verify a cell with specific text displaying in a table based on column header it's belonging 
	 * @param table the table that the cell belonging to
	 * @param columnHeader the column header that the cell belonging to
	 * @param text the text display in cell
	 * @return true/false
	 */
	@Keyword
	public static boolean verifyCellPresentWithText(WebElement table, String columnHeader, String text,	CellTextOptions textOption = CellTextOptions.CONTENT_TEXT, CompareOptions compareOption = CompareOptions.EQUALS,  FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Verify existing a cell with value '${text}' in the table $table"
		boolean found = false
		try{
			table = realocateIfStale(table)
			List<Map<String, Object>>cellsInfo = [[(COLUMN_HEADER): columnHeader, (COMPARE_OPTION): compareOption, (CELL_TEXT_OPTION): textOption, (VALUE): text]]

			WebElement row = getMatchedRow(table, cellsInfo)
			found = row != null
			handleErrorIf(!found, "Could not find any cell with value '$text'", flowControl)

			return found
		}catch(ex){
			handleError(ex, flowControl)
			return found
		}
	}

	private static WebElement getMatchedRow(WebElement table, List<Map<String, Object>> cellsInfo){
		String rowXpath = prepareRowXpath(table, cellsInfo)
		return WebElementHelper.findWebElement(table, By.xpath(rowXpath), FIND_ELEMENT_TIMEOUT)
	}

	private static List<WebElement>getMatchedAndPrecedingRows(WebElement table, List<Map<String, Object>> cellsInfo){
		String rowsXpath = prepareRowXpath(table, cellsInfo)
		rowsXpath = "$rowsXpath|$rowsXpath/preceding-sibling::tr"
		return WebElementHelper.findWebElements(table, By.xpath(rowsXpath), FIND_ELEMENT_TIMEOUT)
	}

	private static boolean isHeaderSeparated(WebElement table){
		String headerXpath = ".//thead/tr"
		WebElement headerRow = WebElementHelper.findWebElement(table, By.xpath(headerXpath), FIND_ELEMENT_TIMEOUT)
		return headerRow != null
	}

	private static String prepareRowXpath(WebElement table, List<Map<String, Object>>cellsInfo){

		String rowXpath = ""
		List<String> columnsXpath = []

		cellsInfo.each{cell ->
			def columnIndex = cell.getAt(COLUMN_INDEX)
			if(!columnIndex){
				String columnHeader = cell.getAt(COLUMN_HEADER)
				columnIndex = getColumnIndex(table, '.', columnHeader)
				handleErrorIf(columnIndex < 0, "Could not find any columns with header '$columnHeader'", null)
			}

			def compareOption = cell.getAt(COMPARE_OPTION)
			if(!compareOption){
				compareOption = CompareOptions.EQUALS
			}

			def cellTextOption = cell.getAt(CELL_TEXT_OPTION)
			if(!cellTextOption){
				compareOption = CellTextOptions.CONTENT_TEXT
			}

			def caseSensitive = cell.getAt(CASE_SENSITIVE)
			if(caseSensitive == null){
				caseSensitive = false
			}

			String value = cell.getAt(VALUE)
			String cellCriteria = makeCellTextComparisonXpath(cellTextOption, compareOption, value, caseSensitive)
			columnsXpath += ".//*[${cellCriteria} and position()=${columnIndex} and (local-name()='td' or local-name()='th')]"
		}
		rowXpath = columnsXpath.join(" and ")
		return "//tr[$rowXpath]"
	}

	static String makeCellTextComparisonXpath(CellTextOptions type, CompareOptions operator, Object value, boolean caseSensitive=false){
		def criteria = ""
		switch(type){
			case CellTextOptions.CONTENT_TEXT:
				criteria = XPathHelper.makeTextComparisionXPath(".", value, operator, caseSensitive)
				break
			case CellTextOptions.INNER_TEXT:
				criteria = XPathHelper.makeTextComparisionXPath("text()", value, operator, caseSensitive)
				break
			case CellTextOptions.SUB_CONTROL_VALUES:
				def subCriteria = XPathHelper.makeTextComparisionXPath("@value", value, operator, caseSensitive)
				criteria = ".//*[not(@type='hidden') and ${subCriteria}]"
				break
			case CellTextOptions.SUB_CONTROL_TEXTS:
				def subCriteria = XPathHelper.makeTextComparisionXPath(".", value, operator, caseSensitive)
				criteria = ".//text()[${subCriteria}]"
				break
			case CellTextOptions.SUB_CONTROL_RADIOS:
				def subCriteria = XPathHelper.makeTextComparisionXPath(".", value, operator, caseSensitive)
				criteria = ".//input[@type='radio' and @checked and ./following-sibling::text()[${subCriteria} and position()=1]]"
				break
			case CellTextOptions.SUB_CONTROL_OPTIONS:
				def subCriteria1 = XPathHelper.makeTextComparisionXPath(".", value, operator, caseSensitive)
				def subCriteria2 = XPathHelper.makeTextComparisionXPath("@value", operator, value, caseSensitive)
				criteria = ".//option[@selected and (${subCriteria1} or ${subCriteria2})]"
				break
			case CellTextOptions.SUB_CONTROL_CHECKS:
				def subCriteria = (value == true || value.toString().trim().equalsIgnoreCase("checked"))?"@checked":"not(@checked)"
				criteria = ".//input[@type='checkbox' and ${subCriteria}]"
				break
			default:
				criteria = XPathHelper.makeTextComparisionXPath(".", value, operator, caseSensitive)
		}
	}

	/**
	 * Get values of cells in a column
	 * @param table the WebElement table which the row belonging to
	 * @param columnHeader: name of column
	 * @return list of values
	 */
	@Keyword
	static List<String> getCellsValueByColumnHeader(WebElement table, String columnHeader, FailureHandling flowControl=FailureHandling.STOP_ON_FAILURE){
		info "Get values of cells in column ${columnHeader}"
		try{
			table = realocateIfStale(table)
			def columnIndex = getColumnIndexByHeader(table, columnHeader, flowControl) - 1
			handleErrorIf (columnIndex <0, "Could not find any columns with header '$columnHeader'", null)
			def cellsXpath = FIND_CELLS_BY_COLUMN_INDEX.replace("@{columnIndex}", columnIndex.toString())
			def listCells = WebElementHelper.findWebElements(null, By.xpath(cellsXpath), FIND_ELEMENT_TIMEOUT)
			def listValues = []
			def rowNum = listCells.size()
			if(rowNum > 0){
				info "Found ${rowNum} row(s)"
				for(cell in listCells){
					def cellValue = WebElementHelper.getTextContent(cell)
					listValues = listValues + cellValue
				}
			}else{
				handleError("Could not find any row", flowControl)
			}

			return listValues
		}
		catch(ex){
			handleError(ex, flowControl)
			return []
		}
	}

	private static def getHeaderIndexes(WebElement table){
		try{
			Map<Integer, String> headerIndexes = [:]
			String locator = ".//tr[1]/child::*"
			List<WebElement> elements = WebElementHelper.findWebElements(table, By.xpath(locator), FIND_ELEMENT_TIMEOUT)
			int index = 1
			elements.each{ e ->
				String header = WebElementHelper.getTextContent(e)
				if(header){
					headerIndexes.putIfAbsent(index, header)
				}
				index ++
			}
			return headerIndexes
		}catch(ex){
			warning "Fail to backup column header info since an error: ${ex.message}"
		}
	}

	private static int getCachedHeaderIndex(String header){
		for(entry in cachedHeaders){
			if(entry.getValue().equalsIgnoreCase(header)){
				return entry.getKey()
			}
		}

		for(entry in cachedHeaders){
			if(entry.getValue().toLowerCase().contains(header.toLowerCase())){
				return entry.getKey()
			}
		}
		return -1
	}
}

