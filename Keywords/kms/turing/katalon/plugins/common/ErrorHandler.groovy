package kms.turing.katalon.plugins.common

import com.kms.katalon.core.exception.StepFailedException
import com.kms.katalon.core.model.FailureHandling
import com.kms.katalon.core.util.KeywordUtil

class ErrorHandler{
	public static void handleError(String message, FailureHandling flowControl) {
		switch(flowControl){
			case FailureHandling.STOP_ON_FAILURE:
				KeywordUtil.markFailedAndStop(message)
				break
			case FailureHandling.CONTINUE_ON_FAILURE:
				KeywordUtil.markFailed(message)
				break
			case FailureHandling.OPTIONAL:
				KeywordUtil.markWarning(message)
				break
			default:
				throw new StepFailedException(message)
		}
	}

	public static void handleError(Exception ex, FailureHandling flowControl){
		handleError(ex.message, flowControl)
	}

	public static void handleErrorIf(boolean expression, String message, FailureHandling flowControl){
		if(expression){
			handleError(message, flowControl)
		}
	}
}
