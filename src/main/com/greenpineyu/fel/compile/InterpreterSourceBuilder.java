package com.greenpineyu.fel.compile;

import com.greenpineyu.fel.context.AbstractContext;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.interpreter.Interpreter;
import com.greenpineyu.fel.parser.FelNode;

public class InterpreterSourceBuilder implements SourceBuilder {
	
	
	
	
	private static final SourceBuilder instance;
		
	public static SourceBuilder getInstance() {
		return instance;
	}

	static{
		instance = new InterpreterSourceBuilder();
	}
	
	
	
	@Override
	public Class<?> returnType(FelContext ctx, FelNode node) {
			try {
				Object value = node.getInterpreter().interpret(ctx, node);
			if (value == FelContext.NOT_FOUND) {
				return Object.class;
			}
				return  AbstractContext.getVarType(value);
			} catch (Exception e) {
			// 有时候在编译时，执行表达式会出错，这里返回Object.class
			return Object.class;
			}
	}

	/**
	 * 用户自定义解析器生成的java代码
	 * 
	 * @param ctx
	 * @param node
	 * @return
	 */
	@Override
	public String source(FelContext ctx, FelNode node) {
		// 用户设置了解释器
//			Interpreter inte = new ProxyInterpreter(node.getInterpreter(), node);
		Interpreter inte = node.getInterpreter();
			SourceBuilder nodeBuilder = node.toMethod(ctx);
			Class<?> type =nodeBuilder.returnType(ctx, node);
			if(type == FelContext.NOT_FOUND_TYPE){
				type = Object.class;
			}
			String code = "("+type.getName()+")";
			String varName = VarBuffer.push(inte,Interpreter.class);
			String nodeVarName = VarBuffer.push(node, FelNode.class);
			code += varName + ".interpret(context," + nodeVarName + ")";
			boolean isNumber = Number.class.isAssignableFrom(type);
			if(isNumber){
			code = "(" + code + ")";
			}
			return code;
	}

}
