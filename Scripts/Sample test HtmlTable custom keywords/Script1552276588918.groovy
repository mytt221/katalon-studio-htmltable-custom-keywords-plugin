import com.kms.katalon.core.annotation.TearDown
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.testcase.TestCaseFactory
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI

import kms.turing.katalon.plugins.helper.XPathHelper.CompareOptions
import kms.turing.katalon.plugins.helper.table.WebTableHelper.CellTextOptions

WebUI.callTestCase(TestCaseFactory.findTestCase("Open browser and go to Home page"), null)

"Navigate to hotels page"
WebUI.navigateToUrl("https://www.phptravels.net/supplier/hotels")

"Identify table web element based on column headers"
table = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.identifyTableByColumnHeaders'(['Image','Name','Stars'], 10,  FailureHandling.CONTINUE_ON_FAILURE)

"Verify a row display inside the table using cells info"
displayed = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.verifyRowDisplayed'(table, ["Name": "Swissotel Le Plaza Basel", "Location": "Messe Basel"], FailureHandling.CONTINUE_ON_FAILURE)

"Get index of column Name by it's header"
columnNameIndex = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.getColumnIndexByHeader'(table, "Name",  FailureHandling.CONTINUE_ON_FAILURE)

"Get index of Column Action by it's css attribute"
columnActionIndex = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.getColumnIndexByAttribute'(table, "class", "xcrud-actions",   FailureHandling.CONTINUE_ON_FAILURE)

"Get row index base on cells info"
rowIndex = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.getRowIndexByCellsInfo'(table, ["Name": "Swissotel Le Plaza Basel", "Location": "Messe Basel"],  FailureHandling.CONTINUE_ON_FAILURE)

"Identify a cell by it's value and column header"
cellName = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.identifyCellByValueAndColHeader'(table, "Name", "Swissotel Le Plaza Basel" ,  FailureHandling.CONTINUE_ON_FAILURE)

"Identify a cell by header and cells info"
cellOrder = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.identifyCellByHeaderAndCellsInfo'(table, "Order", ["Name": "Swissotel Le Plaza Basel", "Location": "Messe Basel"],  FailureHandling.CONTINUE_ON_FAILURE)

"Identify a cell using indexes"
cellCheck = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.identifyCellByIndexes'(table, 1, rowIndex,  FailureHandling.CONTINUE_ON_FAILURE)


"Verify text display inside a cell"
existed = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.verifyCellPresentWithText'(table, 'Name', 'Abbas hotel', CellTextOptions.CONTENT_TEXT, CompareOptions.EQUALS,  FailureHandling.CONTINUE_ON_FAILURE)

"Set text for a cell"
CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.setTextForCell'(cellOrder, "5",  FailureHandling.CONTINUE_ON_FAILURE)

"Get cell's value"
cellText = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.getCellText'(cellName, FailureHandling.CONTINUE_ON_FAILURE)
println cellText


"Check a checkbox inside a cell"
CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.check'(cellCheck, FailureHandling.CONTINUE_ON_FAILURE)

"Uncheck a checkbox inside a cell"
CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.uncheck'(cellCheck, FailureHandling.CONTINUE_ON_FAILURE)

"Identify a cell using indexes"
cellActions = CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.identifyCellByIndexes'(table, columnActionIndex, rowIndex,  FailureHandling.CONTINUE_ON_FAILURE)

"Click on column using it's header"
CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.clickOnColumn'(table, 'Name',  FailureHandling.CONTINUE_ON_FAILURE)

"Click on a link inside a cell"
CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.clickOnLinkInCell'(cellName,'Swissotel Le Plaza Basel',  FailureHandling.CONTINUE_ON_FAILURE)

"Back to hotels page"
WebUI.navigateToUrl("https://www.phptravels.net/supplier/hotels")

"Click on clickable link inside a cell"
CustomKeywords.'kms.turing.katalon.plugins.helper.table.HTMLTableHelper.clickOnClickableControlInCell'(cellActions, "room calender",  FailureHandling.CONTINUE_ON_FAILURE)
