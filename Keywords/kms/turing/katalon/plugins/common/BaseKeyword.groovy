package kms.turing.katalon.plugins.common

import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil
import com.kms.katalon.core.webui.driver.DriverFactory

class BaseKeyword {

	static String chromeBrowser = "Chrome"
	static String firefoxBrowser = "Firefox"
	static String ieBrowser = "IE"
	protected static handleError(String message, FailureHandling flowControl){
		ErrorHandler.handleError(message, flowControl)
	}

	protected static handleError(Exception ex, FailureHandling flowControl){
		ErrorHandler.handleError(ex, flowControl)
	}

	protected static handleErrorIf(boolean expression, String message, FailureHandling flowControl){
		ErrorHandler.handleErrorIf(expression, message, flowControl)
	}

	protected static info(String message){
		KeywordUtil.logInfo(message)
	}

	protected static warning(String message){
		KeywordUtil.markWarning(message)
	}

	protected static error(String message){
		KeywordUtil.markError(message)
	}

	protected static boolean runsOnChrome(){
		return getCurrentBrowser().equalsIgnoreCase(chromeBrowser)
	}

	protected static boolean runsOnFirefox(){
		return getCurrentBrowser().equalsIgnoreCase(firefoxBrowser)
	}

	protected static boolean runsOnIE(){
		return getCurrentBrowser().equalsIgnoreCase(ieBrowser)
	}

	private static String getCurrentBrowser(){
		try{
			return DriverFactory.getExecutedBrowser().toString()
		}catch(ex){
			return ""
		}
	}
}

