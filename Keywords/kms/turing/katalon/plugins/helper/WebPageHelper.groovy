package kms.turing.katalon.plugins.helper

import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.support.ui.ExpectedCondition
import org.openqa.selenium.support.ui.Wait
import org.openqa.selenium.support.ui.WebDriverWait

import com.kms.katalon.core.webui.driver.DriverFactory

public class WebPageHelper {
	static void waitForPageLoad(int timeout){
		try{
			Wait wait = new WebDriverWait(DriverFactory.getWebDriver(), timeout)

			// wait for jQuery to load
			ExpectedCondition<Boolean> jQueryLoad = new ExpectedCondition<Boolean>() {
						@Override
						public Boolean apply(WebDriver driver) {
							try {
								return (Long)((JavascriptExecutor)driver).executeScript("return jQuery.active") == 0
							}
							catch (Exception e) {
								return true
							}
						}
					}

			// wait for Javascript to load
			ExpectedCondition<Boolean> jsLoad = new ExpectedCondition<Boolean>() {
						@Override
						public Boolean apply(WebDriver driver) {
							return ((JavascriptExecutor)driver).executeScript("return document.readyState")
									.toString().equals("complete")
						}
					}

			wait.until(jQueryLoad) && wait.until(jsLoad)
		} catch(ex){

		}
	}
}
