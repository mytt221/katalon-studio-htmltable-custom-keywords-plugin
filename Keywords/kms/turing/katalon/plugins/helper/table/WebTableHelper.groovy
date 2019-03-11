package kms.turing.katalon.plugins.helper.table

import org.openqa.selenium.By
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.ui.Select

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.model.FailureHandling

import kms.turing.katalon.plugins.common.BaseKeyword
import kms.turing.katalon.plugins.common.WebElementExt
import kms.turing.katalon.plugins.helper.WebElementHelper
import kms.turing.katalon.plugins.helper.XPathHelper
import kms.turing.katalon.plugins.helper.XPathHelper.CompareOptions

public class WebTableHelper extends BaseKeyword{

	enum CellTextOptions{
		CONTENT_TEXT,
		INNER_TEXT,
		SUB_CONTROL_VALUES,
		SUB_CONTROL_TEXTS,
		SUB_CONTROL_OPTIONS,
		SUB_CONTROL_RADIOS,
		SUB_CONTROL_CHECKS
	}

	static String COLUMN_HEADER = "columnHeader"
	static String COLUMN_INDEX = "columnIndex"
	static String CELL_TEXT_OPTION = "cellTextOption"
	static String COMPARE_OPTION = "compareOption"
	static String CASE_SENSITIVE = "caseSensitive"
	static String VALUE = "value"
	static int  FIND_ELEMENT_TIMEOUT = 10
	static int  PAGE_LOAD_TIMEOUT = 20
	static Map<Object, Object> cachedElements = [:]

	/**
	 * Set text for input control inside a cell
	 * @param cell the cell object need to set text
	 * @param text the value used for typing into cell
	 */
	@Keyword
	public static void setTextForCell(WebElement cell, String text, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Set '$text' into a input control inside the cell $cell"
		try{
			cell = realocateIfStale(cell)
			handleErrorIf(cell == null, "Could not set text for the cell since it's null", flowControl)
			WebElement textControl = WebElementHelper.findWebElement(cell, By.xpath(".//input[not(@type = 'hidden')]|.//textarea"), FIND_ELEMENT_TIMEOUT)
			handleErrorIf(textControl == null, "Could not find any input control inside the cell $cell", flowControl)
			textControl.clear()
			textControl.sendKeys(text)
		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	/**
	 * Select option of a select box in a cell
	 * @param cell: the cell which the select box belongs to
	 * @param optionText: the label of option in select box
	 * @param isRegex : true if label is regular expression, false if not
	 * @param timeOut: number of seconds to locate element
	 * @param flowControl: failure handling option
	 */
	@Keyword
	public static void selectOptionInCell(WebElement cell, String optionText, boolean isRegex, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Select an item with label '$optionText' in a cell ${cell}. Using regular expression: $isRegex"
		try{
			cell = realocateIfStale(cell)
			WebElement weSelect = WebElementHelper.findWebElement(cell,By.xpath(".//select"), FIND_ELEMENT_TIMEOUT)
			handleErrorIf(weSelect == null, "Could not find any select box inside of the cell $cell", flowControl)
			WebElementHelper.selectOptionByLabel(weSelect, optionText, isRegex)
		}
		catch(ex){
			handleError(ex,flowControl)
		}
	}

	/**
	 * Get display text of the passed in cell
	 * @param cell the cell to get display text
	 * @param cellTextOption the type of text display in the cell. It can be text content, inner text, sub controls value, sub controls text
	 */
	@Keyword
	public static String getCellText(WebElement cell, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE, CellTextOptions cellTextOption = CellTextOptions.CONTENT_TEXT){
		info "Get text/value of the cell $cell"
		String text = ""
		try{
			cell = realocateIfStale(cell)
			switch(cellTextOption){
				case CellTextOptions.CONTENT_TEXT:
					text = WebElementHelper.getTextContent(cell)
					break
				case CellTextOptions.INNER_TEXT:
					String childXpath = ".//child::*"
					WebElement child = WebElementHelper.findWebElement(cell, By.xpath(childXpath), FIND_ELEMENT_TIMEOUT)
					text = child ? WebElementHelper.getTextContent(child) : WebElementHelper.getTextContent(cell)
					break
				case CellTextOptions.SUB_CONTROL_VALUES:
					List<WebElement> textboxes = WebElementHelper.findWebElements(cell, By.xpath(".//input[not(@type = 'hidden')]"), FIND_ELEMENT_TIMEOUT)
					text = getAttributes(textboxes, 'value')
					List<WebElement> selects = WebElementHelper.findWebElements(cell, By.xpath(".//select", FIND_ELEMENT_TIMEOUT))
					List<WebElement> options = getSelectedOptions(selects)
					text += getAttributes(options, 'value')
					break
				case CellTextOptions.SUB_CONTROL_TEXTS:
					List<WebElement> textboxes = WebElementHelper.findWebElements(cell, By.xpath(".//input[not(@type = 'hidden')]"), FIND_ELEMENT_TIMEOUT)
					text = getTexts(textboxes)
					List<WebElement> selects = WebElementHelper.findWebElements(cell, By.xpath(".//select"), FIND_ELEMENT_TIMEOUT)
					List<WebElement> options = getSelectedOptions(selects)
					text += getTexts(options)
					break
				default:
					handleError "We don't support this option '$cellTextOption' currently", flowControl
					break
			}
			return text
		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	private static def getSelectedOptions(List<WebElement>selects){
		List<WebElement>selectedOptions = []
		for(WebElement e in selects){
			Select se = new Select(e)
			if(se){
				WebElement option = se.getFirstSelectedOption()
				if(option){
					selectedOptions += option
				}
			}
		}
		return selectedOptions
	}

	private static String getAttributes(List<WebElement>elements, String attribute){
		String result = ""
		for(WebElement e in elements){
			result += e.getAttribute(attribute)
		}
		return result
	}

	private static String getTexts(List<WebElement>elements){
		String result = ""
		for(WebElement e in elements){
			result += e.text
		}
		return result
	}

	/**
	 * Click on a link with specific text inside a cell of table
	 * @param cell the cell that the link belonging to
	 * @param linkText the text display on the link
	 */
	@Keyword
	public static void clickOnLinkInCell(WebElement cell, String linkText, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		try{
			info "Click on the link by text '$linkText' inside the cell $cell"
			cell = realocateIfStale(cell)
			String textCriteria = XPathHelper.makeTextComparisionXPath('.', linkText, CompareOptions.CONTAINS)
			String linkXpath = ".//*[$textCriteria]"
			WebElement link = WebElementHelper.findWebElement(cell, By.xpath(linkXpath), FIND_ELEMENT_TIMEOUT)
			handleErrorIf(link == null, "Could not find any element with text '$linkText' inside of the cell $cell", null)
			WebElementHelper.clickInView(link)
		}catch(ex){
			handleError(ex, flowControl)
		}
	}


	/**
	 * Click to check a check box inside the cell passed in
	 * @param cell the cell that the check box locating
	 * 
	 */
	@Keyword
	public static void check(WebElement cell, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Click to check the checkbox inside the cell $cell"
		try{
			setCheckboxValueInCell(cell, true)
		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	/**
	 * Click to uncheck a check box inside the cell passed in
	 * @param cell the cell that the check box locating
	 * 
	 */
	@Keyword
	public static void uncheck(WebElement cell, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Click to uncheck the checkbox inside the cell $cell"
		try{
			setCheckboxValueInCell(cell, false)
		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	/**
	 * Check/Uncheck a check box base on input value passed in
	 * @param cell the cell that the link belonging to
	 * @param checked the check box state need to be set
	 */	
	public static void setCheckboxValueInCell(WebElement cell, boolean checked = true){
		info "Set state checked as '$checked' for the checkbox inside the cell $cell"
		WebElement checkbox = null
		try{
			cell = realocateIfStale(cell)
			String nestedElementXpath = ".//input[@type = 'checkbox']"
			checkbox = WebElementHelper.findWebElement(cell, By.xpath(nestedElementXpath), FIND_ELEMENT_TIMEOUT)
			handleErrorIf(!checkbox, "Could not find any checkbox controls inside of the cell $cell", null)
			if(checkbox.selected != checked){
				checkbox.click()
			}
		}catch(ex){
			if(checkbox){
				String nearByElementXpath = ".//following-sibling::*|.//preceding-sibling::*"
				WebElement nearbyElement = WebElementHelper.findWebElement(checkbox, By.xpath(nearByElementXpath), FIND_ELEMENT_TIMEOUT)
				if(nearbyElement){
					nearbyElement.click()
				}
			}
			else{
				throw ex
			}
		}
	}

	/**
	 * Click on a clickable control with specific text displayed inside the cell specific 
	 * @param cell the cell that contains a clickable control
	 * @param caption string display as text, title on the control
	 */
	@Keyword
	public static void clickOnClickableControlInCell(WebElement cell, String caption, FailureHandling flowControl = FailureHandling.STOP_ON_FAILURE){
		info "Click on a clickable control with caption '$caption' inside the cell $cell"
		WebElement checkbox = null
		try{
			cell = realocateIfStale(cell)
			String textXpath = XPathHelper.makeTextComparisionXPath('text()', caption)
			String valueXpath = XPathHelper.makeTextComparisionXPath('@value', caption)
			String titleXpath = XPathHelper.makeTextComparisionXPath('@title', caption)
			def xpath = ".//button[$textXpath]|.//input[(@type='button' or @type='reset' or @type='submit') and $valueXpath]|.//span[$textXpath]|.//a[$textXpath]|.//a[$titleXpath]"
			WebElement nestedElement = WebElementHelper.findWebElement(checkbox, By.xpath(xpath), FIND_ELEMENT_TIMEOUT)
			handleErrorIf(nestedElement == null, "Could not find any element with caption '$caption' inside the cell $cell", flowControl)
			nestedElement.click()
		}catch(ex){
			handleError(ex, flowControl)
		}
	}

	protected static WebElement realocateIfStale(WebElement element){
		try{
			boolean display = element.displayed
			return element
		}catch(StaleElementReferenceException ex){
			info "Realocate the element due to staleness"
			WebElementExt ext = cachedElements.getAt(element)
			if(ext){
				if(ext.parent){
					ext.parent = realocateIfStale(ext.parent)
				}
				WebElement nElement = WebElementHelper.findWebElement(ext.parent, ext.findBy, FIND_ELEMENT_TIMEOUT)
				cachedElements.put(nElement, ext)
				//cachedElements.remove(element)
				return nElement
			}
			return element
		}
	}

	protected static backupElement(WebElement element, WebElement parent, By findBy){
		WebElementExt ex = new WebElementExt(parent, findBy)
		cachedElements.putIfAbsent(element, ex)
	}
}
