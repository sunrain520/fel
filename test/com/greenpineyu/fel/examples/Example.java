package com.greenpineyu.fel.examples;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.greenpineyu.fel.Expression;
import com.greenpineyu.fel.Fel;
import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.FelEngineImpl;
import com.greenpineyu.fel.Foo;
import com.greenpineyu.fel.common.FelBuilder;
import com.greenpineyu.fel.common.ObjectUtils;
import com.greenpineyu.fel.context.AbstractContext;
import com.greenpineyu.fel.context.ContextChain;
import com.greenpineyu.fel.context.FelContext;
import com.greenpineyu.fel.context.MapContext;
import com.greenpineyu.fel.function.CommonFunction;
import com.greenpineyu.fel.function.Function;
import com.greenpineyu.fel.interpreter.ConstInterpreter;
import com.greenpineyu.fel.interpreter.Interpreter;
import com.greenpineyu.fel.optimizer.Interpreters;
import com.greenpineyu.fel.parser.FelNode;

public class Example {

	public static void main(String[] args) {

		helloworld();
		System.out.println("--------------------");
		useVariable();
		System.out.println("--------------------");
		getAttr();
		System.out.println("--------------------");
		visitColl();
		System.out.println("--------------------");
		callMethod();
		System.out.println("--------------------");
		context();
		System.out.println("--------------------");
		contexts();
		System.out.println("--------------------");
		userFunction();
		System.out.println("--------------------");
		userInterpreter();
		System.out.println("--------------------");
		massData();
		System.out.println("--------------------");
		operatorOverload();
		System.out.println("--------------------");
		testCompile();
		System.out.println("--------------------");
		testBigNumber();
		// FelContext ctx = fel.getContext();
		// ctx.set("单价", "5000");
		// ctx.set("数量", new Integer(12));
		// ctx.set("运费", "7500");
		// Object result = fel.eval("单价*数量+运费");
		// System.out.println(result);
	}

	/**
	 * 入门
	 */
	public static void helloworld() {
		Object result = Fel.eval("5000*12+7500");
		System.out.println(result);
	}

	/**
	 * 使用变量
	 */
	public static void useVariable() {
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("单价", 5000);
		ctx.put("数量", 12);
		ctx.put("运费", 7500);
		Object result = Fel.eval("单价*数量+运费", ctx);
		System.out.println(result);
	}
	
	/**
	 * 获取对象属性
	 */
	public static void getAttr(){
		Foo foo = new Foo();
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("foo", foo);
		Map<String,String> m = new HashMap<String,String>();
		m.put("ElName", "fel");
		ctx.put("m", m);
		
		//调用foo.getSize()方法。
		Object result = Fel.eval("foo.size", ctx);
		
		//调用foo.isSample()方法。
		result = Fel.eval("foo.sample", ctx);
		
		//foo没有name、getName、isName方法
		//foo.name会调用foo.get("name")方法。
		result = Fel.eval("foo.name", ctx);
		
		//m.ElName会调用m.get("ElName");
		result = Fel.eval("m.ElName", ctx);
	}

	/**
	 * 调用对象的方法
	 */
	public static void callMethod() {
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("out", System.out);
		Fel.eval("out.println('Hello Everybody'.substring(6))");
	}
	
	/**
	 * 访问数组、集合
	 */
	public static void visitColl(){
		Map<String, Object> ctx = new HashMap<String, Object>();
		
		//数组
		int[] intArray = {1,2,3};
		ctx.put("intArray", intArray);
		//获取intArray[0]
		String exp = "intArray[0]";
		System.out.println(exp + "->" + Fel.eval(exp, ctx));
		
		//List
		List<Integer> list = Arrays.asList(1,2,3);
		ctx.put("list", list);
		//获取list.get(0)
		exp = "list[0]";
		System.out.println(exp + "->" + Fel.eval(exp, ctx));
		
		//集合
		Collection<String> coll = Arrays.asList("a","b","c");
		ctx.put("coll", coll);
		//获取集合最前面的元素。执行结果为"a"
		exp = "coll[0]";
		System.out.println(exp + "->" + Fel.eval(exp, ctx));
		
		//迭代器
		Iterator<String> iterator = coll.iterator();
		ctx.put("iterator", iterator);
		//获取迭代器最前面的元素。执行结果为"a"
		exp = "iterator[0]";
		System.out.println(exp + "->" + Fel.eval(exp, ctx));
		
		//Map
		Map<String,String> m = new HashMap<String, String>();
		m.put("name", "HashMap");
		ctx.put("map", m);
		exp = "map.name";
		System.out.println(exp + "->" + Fel.eval(exp, ctx));
		
		//多维数组
		int[][] intArrays= {{11,12},{21,22}};
		ctx.put("intArrays", intArrays);
		exp = "intArrays[0][0]";
		System.out.println(exp + "->" + Fel.eval(exp, ctx));
		
		//多维综合体，支持数组、集合的任意组合。
		List<int[]> listArray = new ArrayList<int[]>();
		listArray.add(new int[]{1,2,3});
		listArray.add(new int[]{4,5,6});
		ctx.put("listArray", listArray);
		exp = "listArray[0][0]";
		System.out.println(exp + "->" + Fel.eval(exp, ctx));
	}

	/**
	 * 自定义上下文环境
	 */
	public static void context(){
		// 负责提供气象服务的上下文环境
		FelContext ctx = new AbstractContext() {
			@Override
			public Object get(String name) {
				if ("天气".equals(name)) {
					return "晴";
				}
				if ("温度".equals(name)) {
					return 25;
				}
				return null;
			}

		};
		FelEngine fel = new FelEngineImpl(ctx);
		String exp = "'天气:'+天气+';温度:'+温度";
		Object eval = fel.compile(exp, ctx).eval(ctx);
		System.out.println(eval);
	}

	/**
	 * 多层次上下文环境(变量命名空间)
	 */
	public static void contexts() {
		FelEngine fel = getEngine();
		String costStr = "成本";
		String priceStr = "价格";
		FelContext baseCtx = fel.getContext();
		// 父级上下文中设置成本和价格
		baseCtx.set(costStr, 50);
		baseCtx.set(priceStr,100);
		
		String exp = priceStr+"-"+costStr;
		Object baseCost = fel.eval(exp);
		System.out.println("期望利润：" + baseCost);

		FelContext ctx = new ContextChain(baseCtx, new MapContext());
		// 通货膨胀导致成本增加（子级上下文 中设置成本，会覆盖父级上下文中的成本）
		ctx.set(costStr,50+20 );
		Object allCost = fel.eval(exp, ctx);
		System.out.println("实际利润：" + allCost);
	}

	private static FelEngine getEngine() {
		return FelBuilder.engine();
	}
	
	public static void testBigNumber() {
		// 构建大数值计算引擎
		FelEngine fel = FelBuilder.bigNumberEngine();
		FelContext ctx = fel.getContext();
		ctx.set("num", new BigInteger("22222222222222222222222222222222"));
		String input = "111111111111111111111111111111+num";
		Object value = fel.eval(input);
		Object compileValue = fel.compile(input, ctx).eval(ctx);
		System.out.println("大数值计算（解释执行）:" + value);
		System.out.println("大数值计算（编译执行）:" + compileValue);
	}

	public static void testCompile(){
		FelEngine fel = getEngine();
		FelContext ctx = fel.getContext();
		ctx.set("单价", 5000);
		ctx.set("数量", 12);
		ctx.set("运费", 7500);
		Expression exp = fel.compile("单价*数量+运费", ctx);
		Object result = exp.eval(ctx);
		System.out.println(result);
	}
	
	{
		Expression exp = Fel.compile("a+b");
		Map<String, Object> m = new HashMap<String, Object>();
		m.put("a", 1);
		m.put("b", 1);
		exp.eval(m);

	}
	
	public static void userFunction(){
		// 定义hello函数
		Function fun = new CommonFunction() {

			@Override
			public String getName() {
				return "hello";
			}

			/* 
			 * 调用hello("xxx")时执行的代码
			 */
			@Override
			public Object call(Object[] arguments) {
				Object msg = null;
				if(arguments!= null && arguments.length>0){
					msg = arguments[0];
				}
				return ObjectUtils.toString(msg);
			}

		};
		FelEngine e = getEngine();
		// 添加函数到引擎中。
		e.addFun(fun);
		String exp = "hello('fel')";
		// 解释执行
		Object eval = e.eval(exp);
		System.out.println("hello "+eval);
		// 编译执行
		Expression compile = e.compile(exp, null);
		eval = compile.eval((FelContext) null);
		System.out.println("hello "+eval);
	}
	
	
	/**
	 * 
	 */
	public static void testCompileX(){
		FelEngine fel = getEngine();
		String exp = "单价*数量";
		final MutableInt index = new MutableInt(0);

		// 数据库中单价列的记录
		final int[] price = new int[] { 2, 3, 4 };
		// 数据库中数量列的记录
		final double[] number = new double[] { 10.99, 20.99, 9.9 };
		FelContext context = new AbstractContext() {

			@Override
			public Object get(String name) {
				if ("单价".equals(name)) {
					return price[index.intValue()];
				}
				if ("数量".equals(name)) {
					return number[index.intValue()];
				}
				return null;
			}
		};
		Expression compExp = fel.compile(exp, context);
		for (int i = 0; i < number.length; i++) {
			index.setValue(i);
			Object eval = compExp.eval(context);
			System.out.println("总价[" + price[i] + "*" + number[i] + "=" + eval
					+ "]");
		}
	}

	/**
	 * 自定义 解释器
	 */
	public static void userInterpreter() {
		FelEngine fel = getEngine();
		String costStr = "成本";
		FelContext rootContext = fel.getContext();
		rootContext.set(costStr, "60000");
		FelNode node = fel.parse(costStr);
		// 将变量解析成常量
		node.setInterpreter(new ConstInterpreter(rootContext, node));
		System.out.println(node.eval(rootContext));
	}

	/**
	 * 大数据量计算（计算1千万次)
	 */
	public static void massData() {
		FelEngine fel = getEngine();
		final Interpreters opti = new Interpreters();
		final MutableInt index = new MutableInt(0);
		int count = 10*1000*1000;
		final double[] counts = new double[count];
		final double[] prices = new double[count];
		Arrays.fill(counts, 10d);
		Arrays.fill(prices, 2.5d);
		opti.add("单价", new Interpreter() {
			@Override
			public Object interpret(FelContext context, FelNode node) {
				return prices[index.intValue()];
			}
		});
		opti.add("数量", new Interpreter() {
			@Override
			public Object interpret(FelContext context, FelNode node) {
				return counts[index.intValue()];
			}
		});
		Expression expObj = fel.compile("单价*数量", null, opti);
		long start = System.currentTimeMillis();
		Object result = null;
		for (int i = 0; i < count; i++) {
			result = expObj.eval((FelContext) null);
			index.increment();
		}
		long end = System.currentTimeMillis();
		
		System.out.println("大数据量计算:" + result + ";耗时:" + (end - start));
	}

	/**
	 * 操作符重载，使用自定义解释器实现操作符重载
	 */
	public static void operatorOverload() {
		/*
		 * 扩展Fel的+运算符，使其支持数组+数组
		 */
		
		FelEngine fel = getEngine();
		// 单价
		double[] price = new double[] { 2, 3, 4 };
		// 费用
		double[] cost = new double[] { 0.3, 0.3, 0.4 };
		FelContext ctx = fel.getContext();
		ctx.set("单价", price);
		ctx.set("费用", cost);
		String exp = "单价+费用";
		Interpreters interpreters = new Interpreters();
		// 定义"+"操作符的解释方法。
		interpreters.add("+", new Interpreter() {
			@Override
			public Object interpret(FelContext context, FelNode node) {
				List<FelNode> args = node.getChildren();
				double[] leftArg = (double[]) args.get(0).eval(context);
				double[] rightArg = (double[]) args.get(1).eval(context);
				return sum(leftArg)+sum(rightArg);
			}

			// 对数组进行求和
			public double sum(double[] array){
				double d = 0;
				for (int i = 0; i < array.length; i++) {
					d+=array[i];
				}
				return d;
			}
		});
		
		// 使用自定义解释器作为编译选项进行进行编译
		Expression expObj = fel.compile(exp, null, interpreters);
		Object eval = expObj.eval(ctx);
		System.out.println("数组相加:" + eval);
	}


	public static void testSpeed() {
		FelEngine fel = getEngine();
		String exp = "40.52334+60*(21.8144+17*32.663)";
		FelNode node = fel.parse(exp);
		int times = 100 * 1000 * 1000;
		long s1 = System.currentTimeMillis();
		for (int i = 0; i < times; i++) {
			//			double j = 40.52334 + 60 * (21.8144 + 17 * 32.663);
			node.eval((FelContext) null);
		}
		long s2 = System.currentTimeMillis();
		System.out.println("花费的时间:" + (s2 - s1));
	}

}

class ColumnInterpreter implements Interpreter {
	MutableInt index;

	double[] records;

	ColumnInterpreter(MutableInt index, double[] records) {
		this.index = index;
		this.records = records;
	}

	@Override
	public Object interpret(FelContext context, FelNode node) {
		return records[index.intValue()];
	}
}

class MutableInt {
	private int value;

	public MutableInt(int i) {
		this.value = i;
	}

	public int intValue() {
		return value;
	}

	public void setValue(int i) {
		this.value = i;
	}

	public void increment() {
		value++;
	}
}

