package com.greenpineyu.fel.function.operator;

import com.greenpineyu.fel.common.NumberUtil;

public class GreaterThan extends LessThen {
	
	
	
	@Override
	public String getName() {
		return ">";
	}

	/**
	 * 大于 >
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
    @SuppressWarnings({
			"unchecked", "rawtypes" })
	@Override
	public boolean compare(Object left, Object right) {
    	if(left == right){
			return false;
		}
		
		if(left == null || right == null){
			return false;
		}
		
		if(left instanceof Number && right instanceof Number){
			return NumberUtil.toDouble((Number)left)>NumberUtil.toDouble((Number)right);
		}
		
		if (left instanceof Comparable && right instanceof Comparable) {
			return ((Comparable)left).compareTo(right)>0;
		}
		return throwException(left, right);
    }

}
