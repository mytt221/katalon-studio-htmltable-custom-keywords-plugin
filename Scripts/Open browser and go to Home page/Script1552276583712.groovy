import com.kms.katalon.core.model.FailureHandling as FailureHandling
import com.kms.katalon.core.testobject.ObjectRepository
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

urlHome = "https://www.phptravels.net/admin"

username = "admin@phptravels.com"
password = "demoadmin"
"Open browser and navigate to test side"
WebUI.openBrowser(urlHome, FailureHandling.STOP_ON_FAILURE)
"Maximize the browser window"
WebUI.maximizeWindow(FailureHandling.OPTIONAL)
"Wait for page load"
WebUI.waitForElementClickable(ObjectRepository.findTestObject('Object Repository/Login Page/btn_Login'), 10)
"Input username"
WebUI.setText(ObjectRepository.findTestObject('Object Repository/Login Page/txt_Username'), username)
"Input password"
WebUI.setText(ObjectRepository.findTestObject('Object Repository/Login Page/txt_Password'), password)
"Click to login"
WebUI.click(ObjectRepository.findTestObject("Object Repository/Login Page/btn_Login"))
"Wait for login success"
WebUI.waitForElementNotPresent(ObjectRepository.findTestObject('Object Repository/Login Page/btn_Login'), 10)
