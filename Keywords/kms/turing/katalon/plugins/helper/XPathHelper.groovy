package kms.turing.katalon.plugins.helper

import org.apache.commons.lang.StringUtils
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebElement

import com.kms.katalon.core.webui.driver.DriverFactory

import kms.turing.katalon.plugins.common.BaseKeyword

public class XPathHelper extends BaseKeyword{

	enum CompareOptions{
		EQUALS, NOT_EQUAL, STARTS_WITH, ENDS_WITH, CONTAINS
	}

	protected static makeTextComparisionXPath(String attribute, String value, CompareOptions compareOptions = CompareOptions.EQUALS, boolean caseSensitive = false){

		String xpath = ""
		String orginalText = "ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0"
		String replacement = "abcdefghijklmnopqrstuvwxyz"
		String compareValue = StringUtils.normalizeSpace(value)

		String compareThing = "normalize-space($attribute)"
		if(!caseSensitive){
			compareValue = compareValue.toLowerCase()
			compareThing = "translate($compareThing, '$orginalText', '$replacement')"
		}

		compareValue = "'$compareValue'"
		switch(compareOptions){
			case CompareOptions.EQUALS:
				xpath = "$compareThing = $compareValue"
				break
			case CompareOptions.STARTS_WITH:
				xpath = "starts-with($compareThing, $compareValue)"
				break
			case CompareOptions.ENDS_WITH:
				xpath = "substring($compareThing, string-length($compareThing) - string-length($compareValue) + 1) = $compareValue"
				break
			case CompareOptions.CONTAINS:
				xpath = "contains($compareThing, $compareValue)"
				break
			case CompareOptions.NOT_EQUAL:
				xpath = "not($compareThing = $compareValue)"
				break
		}
		return xpath
	}

	static String GetElementXPath(WebElement element) {
		return (String) ((JavascriptExecutor) DriverFactory.getWebDriver()).executeScript(
				"getXPath=function(node)" +
				"{" +
				"if (node.id !== '')" +
				"{" +
				"return '//' + node.tagName.toLowerCase() + '[@id=\"' + node.id + '\"]'" +
				"}" +

				"if (node === document.body)" +
				"{" +
				"return node.tagName.toLowerCase()" +
				"}" +

				"var nodeCount = 0;" +
				"var childNodes = node.parentNode.childNodes;" +

				"for (var i=0; i<childNodes.length; i++)" +
				"{" +
				"var currentNode = childNodes[i];" +

				"if (currentNode === node)" +
				"{" +
				"return getXPath(node.parentNode) + '/' + node.tagName.toLowerCase() +'[' + (nodeCount+1) + ']'"
				+
				"}" +

				"if (currentNode.nodeType === 1 && " +
				"currentNode.tagName.toLowerCase() === node.tagName.toLowerCase())" +
				"{" +
				"nodeCount++" +
				"}" +
				"}" +
				"};" +

				"return getXPath(arguments[0]);", element)
	}
}
