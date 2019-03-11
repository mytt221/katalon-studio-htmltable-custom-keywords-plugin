package kms.turing.katalon.plugins.helper

import java.util.Map

import org.apache.commons.lang.StringUtils
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.Select
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait

import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.webui.driver.DriverFactory

import groovy.time.TimeCategory
import kms.turing.katalon.plugins.common.BaseKeyword

class WebElementHelper extends BaseKeyword{
	static List<WebElement> findWebElements(WebElement parent, By findBy, int timeout = 0){

		def from = DriverFactory.getWebDriver()
		if (timeout == 0){
			timeout = RunConfiguration.getTimeOut()
		}

		if (parent){
			from = parent
		}

		def currentTime = Calendar.getInstance().getTime()
		def endTime = 0
		use(TimeCategory) {
			endTime = currentTime + timeout.seconds
		}

		List<WebElement> elements = []
		while(endTime.after(currentTime)){
			elements = from.findElements(findBy)
			if(!elements){
				sleep(500)
			}
			else{
				break
			}
			currentTime = Calendar.getInstance().getTime()
		}
		return elements
	}

	static WebElement findWebElement(WebElement parent, By findBy, int timeout = 0){
		List<WebElement> elements = findWebElements(parent, findBy, timeout)
		return elements ? elements[0]: null
	}

	static void clickInView(WebElement element, boolean usingJs = false){
		try{
			moveToElement(element)
			if(!runsOnChrome() && usingJs){
				clickUsingJs(element)
			}else{
				element.click()
			}
		}catch(ex){
			warning "Fail to click on element $element due to error: ${ex.message}"
		}
	}

	static void clickUsingJs(WebElement element){
		((JavascriptExecutor) DriverFactory.getWebDriver()).executeScript("arguments[0].click();", element)
	}

	static void moveToElement(WebElement element){
		try{
			if(runsOnIE()){
				jsScrollIntoView(element)
			}
			else{
				WebDriver driver = DriverFactory.getWebDriver()
				Actions action = new Actions(driver)
				action.moveToElement(element).perform()
			}
		}catch(ex){
			warning "Fail to move to the element ${element} due to error: ${ex.message}"
		}
	}

	static void jsScrollIntoView(WebElement element){
		try{
			((JavascriptExecutor) DriverFactory.getWebDriver()).executeScript("arguments[0].click();", element)
		}catch(ex){
			warning "Fail to scroll element ${element} into view using javascript due to error: ${ex.message}"
		}
	}

	static String getTextContent(WebElement element){
		def driver= DriverFactory.getWebDriver()
		def text = ((JavascriptExecutor) driver).executeScript("return arguments[0].textContent;",element);
		return text ? text.toString() : ""
	}

	static void waitForElementClickable(WebElement element, int timeout = 0){

		if(timeout <= 0){
			timeout=RunConfiguration.getTimeOut()
		}
		try{
			def driver = DriverFactory.getWebDriver()
			Wait wait = new WebDriverWait(driver, timeout);
			info "Waiting for element '${element}' to be clickable within ${timeout} seconds"
			def webEl = (WebElement)wait.until(ExpectedConditions.elementToBeClickable(element))
			if (webEl == null){
				warning ("Element '${element}' not found or clickable")
			}
		}
		catch(ex){
			warning ("Fail to wait for element is clickable due to error: ${ex.message}")
		}
	}

	static WebElement waitForElementClickable(By locator, int timeout = 0){

		if(timeout <= 0){
			timeout=RunConfiguration.getTimeOut()
		}
		try{
			def driver = DriverFactory.getWebDriver()
			Wait wait = new WebDriverWait(driver, timeout);
			info "Waiting for element locate by'${locator}' to be clickable within ${timeout} seconds"
			def webEl = (WebElement)wait.until(ExpectedConditions.elementToBeClickable(locator))
			if (webEl == null){
				warning ("Element locates at '${locator}' not found or clickable")
			}
			return webEl
		}
		catch(ex){
			warning ("Fail to wait for element is clickable due to error: ${ex.message}")
			return null
		}
	}

	static void selectOptionByLabel(WebElement element, String labelText, boolean isRegex){
		info "Selecting an option by its label. Label='$labelText'. Using Regular Expression: '$isRegex'"
		def optionElementXpath = ".//option[normalize-space(text())='${labelText}']"
		try{
			if(isRegex){
				def DELIMETER1 ="~!@1"
				def DELIMETER2 ="~!@2"
				def DELIMETER3 ="~!@3"
				def regLabel="${DELIMETER1}${labelText}${DELIMETER2}"
				def outerHtml = element.getAttribute("outerHTML")
				outerHtml = outerHtml.replaceAll("</option>", DELIMETER2 + DELIMETER3).replaceAll("<option.*?>", DELIMETER2 + DELIMETER1).replaceAll("</select>", DELIMETER2 + DELIMETER3)
				String[] match =outerHtml.split(labelText)

				if(match.length <= 1){
					throw new Exception ("Could not find option with label '${labelText}' (regex=true) in select box")
				}

				def index= StringUtils.countMatches(match[0],DELIMETER1)
				optionElementXpath = ".//option[${index+1}]"
				new Select(element).selectByIndex(index)
			}
			else {
				new Select(element).selectByVisibleText(labelText)
			}
		} catch (ex){
			throw new Exception ("Could not find option with label '${labelText}' (regex= ${isRegex}) in select box")
		}
	}
}
