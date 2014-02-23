package com.wi.tool.splitWord;

import static com.wi.tool.library.InitDictionary.IN_SYSTEM;
import static com.wi.tool.library.InitDictionary.status;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.ansj.util.Graph;
import org.ansj.util.MyStaticValue;
import org.ansj.util.WordAlert;

import love.cq.domain.Forest;
import love.cq.splitWord.GetWord;
import love.cq.util.StringUtil;

import com.wi.tool.domain.Term;
import com.wi.tool.domain.TermNature;
import com.wi.tool.domain.TermNatures;
import com.wi.tool.library.UserDefineLibrary;
import com.wi.tool.splitWord.impl.GetWordsImpl;

/**
 * 基本分词+人名识别
 * 
* @For WI Cloud
 * 
 */
public abstract class Analysis {

	/**
	 * 用来记录偏移量
	 */
	public int offe;

	/**
	 * 记录上一次文本长度
	 */
	private int tempLength;

	/**
	 * 分词的类
	 */
	private GetWordsImpl gwi = new GetWordsImpl();

	protected Forest[] forests = null;

	/**
	 * 文档读取流
	 */
	private BufferedReader br;

	protected Analysis() {
	};

	private LinkedList<Term> terms = new LinkedList<Term>();

	/**
	 * while 循环调用.直到返回为null则分词结束
	 * 
	 * @return
	 * @throws IOException
	 */
	private Term term = null;

	public Term next() throws IOException {

		if (!terms.isEmpty()) {
			term = terms.poll();
			term.updateOffe(offe);
			return term;
		}

		String temp = br.readLine();

		while (StringUtil.isBlank(temp)) {
			if (temp == null) {
				return null;
			} else {
				offe = offe + temp.length() + 1;
				temp = br.readLine();
			}

		}

		offe += tempLength;

		// 歧异处理字符串

		analysisStr(temp);

		if (!terms.isEmpty()) {
			term = terms.poll();
			term.updateOffe(offe);
			return term;
		}

		return null;
	}

	/**
	 * 一整句话分词,用户设置的歧异优先
	 * 
	 * @param temp
	 */
	private void analysisStr(String temp) {
		Graph gp = new Graph(temp);
		int startOffe = 0;
		if (UserDefineLibrary.ambiguityForest != null) {
			GetWord gw = new GetWord(UserDefineLibrary.ambiguityForest,
					gp.chars);
			String[] params = null;
			while ((gw.getAllWords()) != null) {
				if (gw.offe > startOffe) {
					analysis(gp, startOffe, gw.offe);
				}
				params = gw.getParams();
				startOffe = gw.offe;
				for (int i = 0; i < params.length; i += 2) {
					gp.addTerm(new Term(params[i], startOffe, new TermNatures(
							new TermNature(params[i + 1], 1))));
					startOffe += params[i].length();
				}
			}
		}
		if (startOffe < gp.chars.length - 1) {
			analysis(gp, startOffe, gp.chars.length);
		}
		List<Term> result = this.getResult(gp);

		terms.addAll(result);
	}

	private void analysis(Graph gp, int startOffe, int endOffe) {
		int start = 0;
		int end = 0;
		char[] chars = gp.chars;

		String str = null;
		char c = 0;
		for (int i = startOffe; i < endOffe; i++) {
			switch (status[chars[i]]) {
			case 0:
				gp.addTerm(new Term(chars[i] + "", i, TermNatures.NULL));
				break;
			case 4:
				start = i;
				end = 1;
				while (++i < endOffe && status[chars[i]] == 4) {
					end++;
				}
				str = WordAlert.alertEnglish(chars, start, end);
				gp.addTerm(new Term(str, start, TermNatures.EN));
				i--;
				break;
			case 5:
				start = i;
				end = 1;
				while (++i < endOffe && status[chars[i]] == 5) {
					end++;
				}
				str = WordAlert.alertNumber(chars, start, end);
				gp.addTerm(new Term(str, start, TermNatures.M));
				i--;
				break;
			default:
				start = i;
				end = i;
				c = chars[start];
				while (IN_SYSTEM[c] > 0) {
					end++;
					if (++i >= endOffe)
						break;
					c = chars[i];
				}

				if (start == end) {
					gp.addTerm(new Term(String.valueOf(c), i, TermNatures.NULL));
					continue;
				}

				gwi.setChars(chars, start, end);
				while ((str = gwi.allWords()) != null) {
					gp.addTerm(new Term(str, gwi.offe, gwi.getTermNatures()));
				}

				/**
				 * 如果未分出词.以未知字符加入到gp中
				 */
				if (IN_SYSTEM[c] > 0 || status[c] > 3) {
					i -= 1;
				} else {
					gp.addTerm(new Term(String.valueOf(c), i, TermNatures.NULL));
				}

				break;
			}
		}
	}

	/**
	 * 将为标准化的词语设置到分词中
	 * 
	 * @param gp
	 * @param result
	 */
	protected void setRealName(Graph graph, List<Term> result) {

		if (!MyStaticValue.isRealName) {
			return;
		}

		String str = graph.realStr;

		for (Term term : result) {
			term.setRealName(str.substring(term.getOffe(), term.getOffe()
					+ term.getName().length()));
		}
	}

	protected List<Term> parseStr(String temp) {
		// TODO Auto-generated method stub
		analysisStr(temp);
		return terms;
	}

	protected abstract List<Term> getResult(Graph graph);

	public abstract class Merger {
		public abstract List<Term> merger();
	}

	/**
	 * 重置分词器
	 * 
	 * @param br
	 */
	public void resetContent(BufferedReader br) {
		this.offe = 0;
		this.tempLength = 0;
		this.br = br;
	}
}