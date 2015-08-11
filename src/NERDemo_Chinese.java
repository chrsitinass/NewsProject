import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;

import java.io.IOException;
import java.util.List;

import org.ansj.domain.Term;

/**
 * This is a demo of calling CRFClassifier programmatically.
 * <p>
 * Usage:
 * {@code java -mx400m -cp "stanford-ner.jar:." NERDemo [serializedClassifier [fileName]] }
 * <p>
 * If arguments aren't specified, they default to
 * classifiers/english.all.3class.distsim.crf.ser.gz and some hardcoded sample
 * text.
 * <p>
 * To use CRFClassifier from the command line:
 * </p>
 * <blockquote>
 * {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -textFile [file] }
 * </blockquote>
 * <p>
 * Or if the file is already tokenized and one word per line, perhaps in a
 * tab-separated value format with extra columns for part-of-speech tag, etc.,
 * use the version below (note the 's' instead of the 'x'):
 * </p>
 * <blockquote>
 * {@code java -mx400m edu.stanford.nlp.ie.crf.CRFClassifier -loadClassifier [classifier] -testFile [file] }
 * </blockquote>
 * 
 * @author Jenny Finkel
 * @author Christopher Manning
 */

public class NERDemo_Chinese {
	/*private AbstractSequenceClassifier<CoreLabel> classifier;
	private String serializedClassifier = "classifiers\\chinese.misc.distsim.crf.ser.gz";
	private Segmentation Seg = new Segmentation();
	NERDemo_Chinese nerC;*/
	public static void main(String[] args) throws Exception {
		String serializedClassifier = "classifiers/chinese.misc.distsim.crf.ser.gz";
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
		Segmentation Seg = new Segmentation() ;
		
		String s = "奥巴马在白宫会见习近平";
		NERDemo_Chinese nerC = new NERDemo_Chinese();
		String NERInput = Seg.getSegArray(s);
		
		// nerC.printAllFormatResult(classifier, NERInput);
		// String result = nerC.getXMLResult(classifier, NERInput);
		// String result = nerC.getXMLResult(s);
		String result = nerC.getStringRes(classifier, NERInput);
		
		// List<List<CoreLabel>> CompletNERResult = nerC.getCompletResult(classifier, NERInput);
		
	}
	/*
     * public NERDemo_Chinese() throws ClassCastException, ClassNotFoundException, IOException{
     *     classifier = CRFClassifier.getClassifier(serializedClassifier);
     *     nerC = new NERDemo_Chinese();
     * }
     * public String getContentWithURL(String content){
     *     String result = this.nerC.getStringRes(classifier, content);
     *     String NERInput = Seg.getSegArray(content);
     *     result = nerC.getStringRes(classifier, NERInput);
     *     return result;
     * }
	 */
	// get format 1 result
	public String getSimpleResult(AbstractSequenceClassifier<CoreLabel> classifier, String inputString){
		return classifier.classifyToString(inputString);
	}
	
	public List<List<CoreLabel>> getCompletResult(AbstractSequenceClassifier<CoreLabel> classifier, String inputString){
		List<List<CoreLabel>> result = classifier.classify(inputString);
		return result;
	}
	
	public String getXMLResult(AbstractSequenceClassifier<CoreLabel> classifier, String str){
		String result = null;
		result = classifier.classifyToString(str, "xml", true);
		return result;
	}
	
	public String getXMLResult(String s) throws Exception{
		String serializedClassifier = "classifiers/chinese.misc.distsim.crf.ser.gz";		
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
		NERDemo_Chinese nerC = new NERDemo_Chinese();
		Segmentation Seg = new Segmentation();
		String NERInput = Seg.getSegArray(s);
		
		//nerC.printAllFormatResult(classifier, NERInput);
		String result = nerC.getXMLResult(classifier, NERInput);

		return result;
	}
	
	public void getNERItem(List<List<CoreLabel>> result, String flag){
		if(flag.equalsIgnoreCase("PERSON")){
			for(List<CoreLabel> iter : result){
				for(CoreLabel cl : iter){
					System.out.println(cl.ner());
					System.out.println(cl.beginPosition());
				}
			}
		}
		else if(flag.equalsIgnoreCase("GPE")){
			
		}
		else if(flag.equalsIgnoreCase("ALL")){
			
		}
	}
	public String getStringRes(AbstractSequenceClassifier<CoreLabel> classifier, String str){
		try {
			return classifier.classifyToString(str);
		}
		catch (Exception e) {
			System.out.println(e);
			return "";
		}
	}
	
	public void printAllFormatResult(AbstractSequenceClassifier<CoreLabel> classifier, String str){			
		System.out.println("Format 1 :\n" + classifier.classifyToString(str));
		System.out.println("Format 2 :\n" + classifier.classifyToString(str, "slashTags",true));
		System.out.println("Format 3 :\n" + classifier.classifyWithInlineXML(str));
		System.out.println("Format 4 :\n" + classifier.classifyToString(str, "xml", true));
		System.out.println("Format 5: \n");
		
		int i = 0;
		for (List<CoreLabel> lcl : classifier.classify(str)) {
			for (CoreLabel cl : lcl) {
				System.out.print(i++ + ": ");
				System.out.println(cl.toShorterString());
			}
		}
	}
}
	
	