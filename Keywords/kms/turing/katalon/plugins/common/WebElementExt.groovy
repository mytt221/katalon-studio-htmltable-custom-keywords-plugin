package kms.turing.katalon.plugins.common

import org.openqa.selenium.By
import org.openqa.selenium.WebElement

class WebElementExt {
	WebElement parent
	By findBy

	public WebElementExt(WebElement p, By locator){
		parent = p
		findBy = locator
	}
}
