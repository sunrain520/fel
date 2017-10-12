package com.greenpineyu.fel.context;


/**
 * 变量
 * @author yuqingsong
 */
public class Var {
	
	public static Var notFound() {
		Var v = new Var("NOT_FOUND", null, Undefined.class);
		v.setValue(v);
		return v;
	}

	public Var(String name,Object value,Class<?> type){
		this.name = name;
		this.value = value;
		this.type = type;
//		if(type == null){
//			// 如果没有指定type,将type设置成value.getClass。
////			setTypeByValue(value);
//		}
	}
	private Class<?> getTypeByValue() {
		return  value!=null?value.getClass():FelContext.NULL.getClass();
//		this.type = getTypeByValue;
	}
	public Var(String name,Object value){
		this(name,value,null);
	}

	/**
	 * 变量名称
	 */
	private String name;
	
	/**
	 * 变量值
	 */
	private Object value;
	
	/**
	 * 变量值
	 */
	private Class<?> type;
	
	
	public Class<?> getType() {
		return type!=null?type:getTypeByValue();
	}

	public void setType(Class<?> type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		if (this == FelContext.NOT_FOUND) {
			return "undefined var";
		}
		return "var[" + name + "=" + value + "]";
	}

	public static void main(String[] args) {
	}
	

}
