package com.greenpineyu.fel.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

import com.greenpineyu.fel.FelEngine;
import com.greenpineyu.fel.common.Callable;
import com.greenpineyu.fel.exception.FelException;
import com.greenpineyu.fel.exception.ParseException;
import com.greenpineyu.fel.function.Function;
import com.greenpineyu.fel.function.operator.Dot;

/**
 * 使用Antlr进行语法分析
 * @author yuqingsong
 * 
 */
public class AntlrParser implements Parser {
	
	public static final	Callable<Boolean, FelNode> funFilter = new Callable<Boolean, FelNode>() {
		@Override
		public Boolean call(FelNode... node) {
			FelNode n = node[0];
			if(n == null){
				return false;
			}
			boolean isFun = n instanceof FunNode;
			if(isFun){
				if (n instanceof CommonTree) {
					CommonTree treeNode = (CommonTree) n;
					CommonTree p = treeNode.parent;
					if(p!=null){
						if(Dot.DOT.equals(p.getText())){
							// 点运算符后的函数节点不是真正意义上的变量节点。
							isFun = p.getChildren().get(0)==n;
						}
					}
				}
			}
			return isFun;
		}
	};
	
	private final FelEngine engine;
	private final NodeAdaptor adaptor;
	
	public AntlrParser(FelEngine engine){
		this(engine, null);
	}

	public AntlrParser(FelEngine engine, NodeAdaptor adaptor) {
		this.engine = engine;
		if (adaptor == null) {
			adaptor = new NodeAdaptor();
		}
		this.adaptor = adaptor;
	}
	
	public FelNode parse(String exp) {
		if (exp == null || "".equals(exp)) {
			return null;
		}
		ByteArrayInputStream is = new ByteArrayInputStream(exp.getBytes());
		ANTLRInputStream input = null;
		try {
			input = new ANTLRInputStream(is);
		} catch (IOException e) {
			throw new ParseException(FelException.getCauseMessage(e), e);
		}
		FelLexer lexer = new FelLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		FelParser parser = new FelParser(tokens);
		parser.setTreeAdaptor(adaptor);
		ParserRuleReturnScope r = null;
		try {
			r = parser.program();
		} catch (RecognitionException e) {
			throw new ParseException(e.getMessage(), e);
		}
		if (r != null) {
			Object tree = r.getTree();
			if (tree instanceof FelNode) {
				initFun((FelNode)tree);
				return (FelNode) tree;
			}
		}
		return null;
	}
	
	public void initFun(FelNode node){
		List<FelNode> nodes = AbstFelNode.getNodes(node, null);
		if(nodes!=null){
			for (FelNode n : nodes) {
				n.setEngine(this.engine);
				if (n instanceof FunNode) {
					FunNode funNode = (FunNode) n;
					funNode.initFun(this.engine.getFunMgr());
					Function fun = funNode.getFun();
					if (fun instanceof Dot) {
						Dot dot = (Dot) fun;
						dot.setSecurityMgr(this.engine.getSecurityMgr());
					}
				}
			}
		}
	}

	@Override
	public boolean verify(String exp) {
		try {
			parse(exp);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
