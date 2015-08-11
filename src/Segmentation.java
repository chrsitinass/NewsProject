import java.util.List;
import java.util.Vector;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.BaseAnalysis;
import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;


public class Segmentation {
	public static void main(String[] args){
		/*String s = "车主演示最牛停车";
		Segmentation Seg = new Segmentation();
		Seg.getSegArray(s);*/
		String s = "奥斯丁'中国";
		System.out.println(s.replaceAll("'","\\\\'"));
	}
	
	public String getSegArray (String s){
		if (s.length() == 0) return s;
		List<Term> parse = NlpAnalysis.parse(s);
		StringBuffer segResult = new StringBuffer();
		//System.out.println(s);
		for(Term iter: parse){
			String segItem = iter.toString();
			//System.out.println(segItem);
			//System.out.println(segItem);
			if (segItem.indexOf("/")!=-1){
				segItem = segItem.substring(0, segItem.indexOf("/"));
			//System.out.println(segItem);
				segResult.append(segItem + " ");
			}
			else segResult.append(segItem);
		}
		//System.out.println(segResult.toString());
		return segResult.toString();
	}
}
