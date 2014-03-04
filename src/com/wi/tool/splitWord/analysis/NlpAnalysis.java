package com.wi.tool.splitWord.analysis;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

import org.ansj.util.Graph;
import org.ansj.util.MyStaticValue;
import org.ansj.util.WordAlert;

import love.cq.domain.Forest;

import org.ansj.app.crf.SplitWord;
import com.wi.tool.dic.LearnTool;
import com.wi.tool.domain.NewWord;
import com.wi.tool.domain.Term;
import com.wi.tool.library.InitDictionary;
import com.wi.tool.library.NatureLibrary;
import com.wi.tool.recognition.AsianPersonRecognition;
import com.wi.tool.recognition.NatureRecognition;
import com.wi.tool.recognition.NewWordRecognition;
import com.wi.tool.recognition.NumRecognition;
import com.wi.tool.recognition.UserDefineRecognition;
import com.wi.tool.splitWord.Analysis;

/**
 * 
* @For WI Cloud
 * 
 */
public class NlpAnalysis extends Analysis {

	private LearnTool learn = null;

	private static final SplitWord DEFAULT_SLITWORD = MyStaticValue
			.getCRFSplitWord();

	@Override
	protected List<Term> getResult(final Graph graph) {
		// TODO Auto-generated method stub

		Merger merger = new Merger() {
			@Override
			public List<Term> merger() {
				// TODO Auto-generated method stub
				graph.walkPath();

				// 数字发现
				if (graph.hasNum) {
					NumRecognition.recognition(graph.terms);
				}

				// 词性标注
				List<Term> result = getResult();
				new NatureRecognition(result).recognition();
				if (learn == null) {
					learn = new LearnTool();
				}
				learn.learn(graph);

				// 通过crf分词
				List<String> words = DEFAULT_SLITWORD.cut(graph.chars);
				for (String word : words) {
					word = word.replaceAll("[\\pP‘’“”]", "");
					if (word.length() < 2 || InitDictionary.isInSystemDic(word)
							|| WordAlert.isRuleWord(word)) {
						continue;
					}
					//删除所有标点,添加新词
					learn.addTerm(new NewWord(word, NatureLibrary
							.getNature("nw"), -word.length()));
				}

				// 用户自定义词典的识别
				new UserDefineRecognition(graph.terms, forests).recognition();
				graph.walkPathByScore();

				// 进行新词发现
				new NewWordRecognition(graph.terms, learn).recognition();
				graph.walkPathByScore();

				// 修复人名左右连接
				AsianPersonRecognition.nameAmbiguity(graph.terms);

				// 优化后重新获得最优路径
				result = getResult();

				// 激活辞典
				for (Term term : result) {
					learn.active(term.getName());
				}

				setRealName(graph, result);

				return result;
			}

			private List<Term> getResult() {
				// TODO Auto-generated method stub
				List<Term> result = new ArrayList<Term>();
				int length = graph.terms.length - 1;
				for (int i = 0; i < length; i++) {
					if (graph.terms[i] == null) {
						continue;
					}
					result.add(graph.terms[i]);
				}
				return result;
			}
		};
		return merger.merger();
	}

	private NlpAnalysis() {
	};

	/**
	 * 用户自己定义的词典
	 * 
	 * @param forest
	 */

	public NlpAnalysis(Forest... forests) {
		this.forests = forests;
	}

	public NlpAnalysis(LearnTool learn, Forest... forests) {
		this.forests = forests;
		this.learn = learn;
	}

	public NlpAnalysis(BufferedReader reader, Forest... forests) {
		this.forests = forests;
		super.resetContent(reader);
	}

	public NlpAnalysis(BufferedReader reader, LearnTool learn,
			Forest... forests) {
		this.forests = forests;
		this.learn = learn;
		super.resetContent(reader);
	}

	public static List<Term> parse(String str) {
		return new NlpAnalysis().parseStr(str);
	}

	public static List<Term> parse(String str, Forest... forests) {
		return new NlpAnalysis(forests).parseStr(str);
	}

	public static List<Term> parse(String str, LearnTool learn,
			Forest... forests) {
		return new NlpAnalysis(learn, forests).parseStr(str);
	}
}
