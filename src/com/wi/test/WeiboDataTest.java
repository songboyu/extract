package com.wi.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSON;
import net.sf.json.JSONObject;


import com.wi.main.dic.LearnTool;
import com.wi.main.domain.Term;
import com.wi.main.splitWord.analysis.NlpAnalysis;
import com.wi.main.util.MyStaticValue;
import com.wi.main.xml.ToolXmlBySAX;


public class WeiboDataTest {
	private static List<HashMap<String, String>> list = null;
	public static void main(String[] args) {
		FileInputStream article = null;
		PrintStream ps = null;
		try {
			MyStaticValue.LIBRARYLOG.info("开始读取数据");
			article = new FileInputStream(new File(
					"E:/数据/dataset_605070/605070/NLPIR微博内容语料库.xml"));
			FileOutputStream out = new FileOutputStream("E:/数据/result.json");
			ps = new PrintStream(out);
			ps.print("{\"test\":[");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		list = ToolXmlBySAX.readXml(article, "article");
		MyStaticValue.LIBRARYLOG.info("数据读取完成！");
		int i = 1;
		List<Term> terms = null;
		//		KeyWordComputer keyWordComputer = new KeyWordComputer(10);

		String input = null;
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		JSONObject json = new JSONObject();
		for(HashMap<String, String> p : list){
			input = p.get("article");

			LearnTool learnTool = new LearnTool();
			if(input == null || input.equals("越体越中意"))
				continue;

			terms = NlpAnalysis.parse(input, learnTool);
			map = newWordsToMap(learnTool);
			if(!map.isEmpty()){
				System.out.println(p);
				
				p.clear();
				p.put("article-"+(i++),input);

				json.clear();
				json.putAll(p);
				json.putAll(map);

				ps.println((JSON)json+",");
			}
		}
		ps.print("]}");
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
			map.put("new", value);
		}
		return map;
	}
}
