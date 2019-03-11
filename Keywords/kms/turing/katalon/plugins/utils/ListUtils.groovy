package kms.turing.katalon.plugins.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.regex.Pattern
import java.util.regex.Matcher

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.model.FailureHandling
import kms.turing.katalon.plugins.common.BaseKeyword

class ListUtils extends BaseKeyword {

	/**
	 * Verify a List is sorted or not
	 * @param list the list of value need to verify
	 * @param asc sorting direction as ascending or not
	 * @return true/false
	 */
	public static <T extends Comparable<? super T>> boolean isSorted(List<T> list, boolean asc, boolean caseSensitive) {
		if (list){
			for (int i = 1; i < list.size(); i++) {
				if(asc){
					if(!caseSensitive){
						if (list.get(i-1).compareToIgnoreCase(list.get(i)) > 0){
							return false
						}
					}else{
						def pre = list.get(i-1)
						def cur = list.get(i)
						if (list.get(i-1).compareTo(list.get(i)) > 0){
							return false
						}
					}
				}
				else{
					if(!caseSensitive){
						if (list.get(i).compareToIgnoreCase(list.get(i-1)) > 0){
							return false
						}
					}else{
						if (list.get(i).compareTo(list.get(i-1)) > 0){
							return false
						}
					}
				}
			}
		}
		return true
	}

	static def convertList(List<Object> listValues){
		def sortDataType = ''
		def listConvertedValues = []
		for(value in listValues){
			if(!value.isNumber()){
				sortDataType = 'string'
				break
			}
			else
				sortDataType = 'number'
		}
		switch(sortDataType){
			case 'string':
				listConvertedValues = listValues
				break
			case 'number':
				for(value in listValues){
					def numberValue = value.toDouble()
					listConvertedValues = listConvertedValues + numberValue
				}
				break
		}
		return listConvertedValues
	}
}
