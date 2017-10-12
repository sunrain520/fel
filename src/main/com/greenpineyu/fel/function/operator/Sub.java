package com.greenpineyu.fel.function.operator;

import java.util.List;

import com.greenpineyu.fel.common.NumberUtil;
import com.greenpineyu.fel.common.ReflectUtil;
import com.greenpineyu.fel.compile.FelMethod;
import com.greenpineyu.fel.compile.SourceBuilder;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.exception.EvalException;
import com.greenpineyu.fel.function.StableFunction;
import com.greenpineyu.fel.parser.FelNode;

public class Sub extends StableFunction {


	private void appendArg(StringBuilder sb, SourceBuilder argMethod,FelContext ctx,FelNode node) {
//		Class<?> t = argMethod.returnType(ctx, node);
		sb.append("(");
//		if (ReflectUtil.isPrimitiveOrWrapNumber(t)||FelMethod.isUndefinedType(t)) {
//			// 数值型和字符型时，直接添加
//			sb.append(argMethod.source(ctx, node));
//		//} else	if (CharSequence.class.isAssignableFrom(t)) {
//			// FIXME 处理1-"1"的
//		}else{
			sb.append(argMethod.source(ctx, node));
//		}
		sb.append(")");
	}

	public FelMethod toMethod(FelNode node, FelContext ctx) {
		List<FelNode> children = node.getChildren();
		StringBuilder sb = new StringBuilder();
		Class<?> type = null;
		if (children.size() == 2) {
			
			
			//left
			FelNode left = children.get(0);
			SourceBuilder lm = left.toMethod(ctx);
			Class<?> leftType = lm.returnType(ctx, left);
			
			//right
			FelNode right = children.get(1);
			SourceBuilder rm = right.toMethod(ctx);
			Class<?> rightType = rm.returnType(ctx, right);
				if(ReflectUtil.isPrimitiveOrWrapNumber(leftType)
						&&ReflectUtil.isPrimitiveOrWrapNumber(rightType)){
					appendArg(sb, lm,ctx,left);
					
					sb.append("-");
					appendArg(sb, rm,ctx,right);
					type = NumberUtil.arithmeticClass(leftType, rightType);
				}else{
					Add.addCallCode(node, sb,this,"subObject");
					appendArg(sb,lm,ctx,left);
					sb.append(",");
					appendArg(sb,rm,ctx,right);
					sb.append(")");
				}
//			}
		} else if (children.size() == 1) {
			FelNode right = children.get(0);
			SourceBuilder rm = right.toMethod(ctx);
			Class<?> rightType = rm.returnType(ctx, right);
			if (ReflectUtil.isPrimitiveOrWrapNumber(rightType)) {
				type = rightType;
				sb.append("-");
				appendArg(sb, rm, ctx, right);
			} else {
				Add.addCallCode(node, sb, this,"subObject");
				appendArg(sb, rm, ctx, right);
				sb.append(")");
				type = Number.class;
			}
		}
//		sb.append("-");
//		SourceBuilder rm = right.toMethod(ctx);
//		appendArg(sb, rm,ctx,right);
		FelMethod m = new FelMethod(type, sb.toString());
		return m;
	}

	public String getName() {
		return "-";
	}

	public Object call(FelNode node, FelContext context) {
		List<FelNode> children = node.getChildren();
		if (children.size() == 2) {
			FelNode left = children.get(0);
			Object leftValue = left.eval(context);
			FelNode right = children.get(1);
			Object rightValue = right.eval(context);
			/*
			if (leftValue instanceof Number && rightValue instanceof Number) {
				double l = NumberUtil.toDouble(leftValue);
				double r = NumberUtil.toDouble(rightValue);
				return NumberUtil.parseNumber(l - r);
//				if (NumberUtil.isFloatingPoint(left)
//						|| NumberUtil.isFloatingPoint(right)) {
//				}
//				return NumberUtil.parseNumber(((Number) leftValue).longValue()
//						- ((Number) rightValue).longValue());
			}
			throw new EvalException("执行减法出错，参数必须是数值型");
			*/
			return subObject(node,leftValue,rightValue);
		}
		if (children.size() == 1) {
			FelNode right = children.get(0);
			Object rightValue = right.eval(context);
			/*
			if (rightValue instanceof Number) {
				if (NumberUtil.isFloatingPoint(rightValue)) {
					return NumberUtil.toDouble(rightValue) * -1;
				}
				return NumberUtil.parseNumber(((Number) rightValue).longValue() * -1);
			}
			throw new EvalException("执行减法出错，参数必须是数值型");
			*/
			return subObject(node,rightValue);
		}
		throw new EvalException("执行减法出错，参数长度必须是1或2");
	}
	
	
	/**
	 * 请勿修改方法签名，动态生成的代码会用到此方法
	 * @param text
	 * @param param
	 * @return
	 */
	public Object subObject(FelNode node,Object param){
		if(param == null){
			throw new EvalException("执行["+Add.getNodeText(node)+"]操作失败，参数["+param+"]不能为空。");
		}
		if(param instanceof Number){
			return NumberUtil.parseNumber(-((Number) param).doubleValue());
		}else{
			throw new EvalException("执行["+Add.getNodeText(node)+"]操作失败，参数["+param+"]必须是数值型。");
		}
	}
	
	/**
	 * 请勿修改方法签名，动态生成的代码会用到此方法
	 * @param text
	 * @param left
	 * @param right
	 * @return
	 */
	public Object subObject(FelNode node,Object left,Object right){
		if(left == null||right==null){
			throw new EvalException("执行["+Add.getNodeText(node)+"]操作失败，参数["+left+","+right+"]必须是数值型。");
		}
		Class<?> leftType = left.getClass();
		Class<?> rightType = right.getClass();
		if(ReflectUtil.isPrimitiveOrWrapNumber(leftType)
				&&ReflectUtil.isPrimitiveOrWrapNumber(rightType)){
			return NumberUtil.parseNumber(NumberUtil.toDouble((Number)left)-NumberUtil.toDouble((Number)right));
		}else{
			throw new EvalException("执行["+Add.getNodeText(node)+"]操作失败，参数["+left+","+right+"]必须是数值型。");
		}
		
	}
	
	public static void main(String[] args) {
		int a = -(1);
		System.out.println(a);
	}

}
