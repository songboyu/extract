package com.wi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.wi.tool.dic.LearnTool;
import com.wi.tool.domain.Term;
import com.wi.tool.splitWord.analysis.NlpAnalysis;

public class ExtractServlet extends HttpServlet {

	private enum ExtractMethod {
		SEG,NEW,KEY, ALL 
	}


	@Override
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		super.init();
		NlpAnalysis.parse("Hello");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		process(req, resp);
	}

	private void process(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {

		req.setCharacterEncoding("UTF-8");
		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		
		String strMethod = null;
		String input =null;

		if(req.getParameter("input") == null ){
			out.print("Hello");
			return;
		}else{ 
			input = new String(req.getParameter("input").getBytes("ISO8859_1"), "UTF-8");
		}
		if(req.getParameter("method") == null){
			strMethod = "ALL";
		}else{ 
			strMethod = new String(req.getParameter("method").getBytes("ISO8859_1"), "UTF-8");
		}
		ExtractMethod method =  ExtractMethod.valueOf(strMethod.toUpperCase());
		LearnTool learnTool = new LearnTool();
		Collection<Keyword> keyWords = null;
		List<Term> terms = null;


		if (method == ExtractMethod.ALL) {
			KeyWordComputer keyWordComputer = new KeyWordComputer(10);
			keyWords = keyWordComputer.computeArticleTfidf(input);
			terms = NlpAnalysis.parse(input, learnTool);
		}
		else if (method == ExtractMethod.SEG) {
			terms = NlpAnalysis.parse(input);
		}
		else if (method == ExtractMethod.NEW) {
			terms = NlpAnalysis.parse(input, learnTool);
		}
		else if(method == ExtractMethod.KEY){
			KeyWordComputer keyWordComputer = new KeyWordComputer(10);
			keyWords = keyWordComputer.computeArticleTfidf(input);
		}

		switch (method) {
		case ALL:
			JSONObject json = new JSONObject();
//			json.putAll(termsToMap(terms));
			json.putAll(newWordsToMap(learnTool));
			json.putAll(keyWordsToMap(keyWords));
			out.print((JSON) json);
			break;
		case SEG:
			out.print(JSONSerializer.toJSON(termsToMap(terms)));
			break;
		case NEW:
			out.print(JSONSerializer.toJSON(newWordsToMap(learnTool)));
			break;
		case KEY:
			out.print(JSONSerializer.toJSON(keyWordsToMap(keyWords)));
			break;
		}
	}

	private static Map<String, List<String>> newWordsToMap(LearnTool learnTool) {
		// TODO Auto-generated method stub
		List<String> value = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		List<Entry<String, Double>> topTree = learnTool.getTopTree(0);
		if (topTree != null) {
			for (Map.Entry<String, Double> n : topTree) {
				String tmp = n.getKey();
				if (!tmp.substring(0, 1).matches("\\d*")) {
					value.add(tmp);
				}
			}
			map.put("NewWords", value);
		}
		return map;
	}

	//	private static Map<String, Map<String, String>> keyWordsWithScoreToMap(
	//			Collection<Keyword> keyWords) {
	//		// TODO Auto-generated method stub
	//		Map<String, String> value = new HashMap<String, String>();
	//		Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
	//
	//		for (Keyword k : keyWords) {
	//			DecimalFormat df=new DecimalFormat(".##");
	//			value.put(k.getName(), df.format(k.getScore()));
	//		}
	//		map.put("KeyWords", value);
	//		return map;
	//	}

	private static Map<String, List<String>> keyWordsToMap(
			Collection<Keyword> keyWords) {
		// TODO Auto-generated method stub
		List<String> value = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();

		for (Keyword k : keyWords) {
			String tmp = k.getName();
			value.add(tmp);
		}
		map.put("KeyWords", value);
		return map;
	}
	private static Map<String, List<String>> termsToMap(List<Term> terms) {
		// TODO Auto-generated method stub
		List<String> value = new ArrayList<String>();
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for (Term term : terms) {
			String tmp = term.getName();
			value.add(tmp);
		}
		map.put("Segmentation", value);
		return map;
	}
}
