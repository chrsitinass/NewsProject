import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.NlpAnalysis;

public class EntityLinking {
	public static void main(String [] args)
	{
		String folder = "E:/StanfordNER/data/";
		BufferedReader br = getBufferedReader(
				folder + "news3.txt");
		StringBuffer sb = new StringBuffer();
		String oneLine = "";
		try {
			while((oneLine = br.readLine()) != null)
			{
				sb.append(oneLine + "\n");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EntityLinking one = new EntityLinking("172.31.19.9", "root", "");
		//String result = one.FillLinks(sb.toString(), 0, 2);
		//System.out.println(result);
		/*
		sb.setLength(0);
		sb.append("<html>");
		sb.append("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		sb.append("<head><title>Html Test</title></head><body>");
	    sb.append(result);
	    sb.append("</body></html>");
	    */
		//System.out.println(result);
		//deleteFile(folder + "test.html");
		//addFile(sb.toString(), folder + "test.html");
	}

	static Pattern tag = Pattern.compile("<wi( ([^ >]{1,}))+>");
	private Mysql mysql;
	/**
	 * inputType = 0 : PlainString
	 * inputType = 1 : NERedString
	 * outputType = 0 : html
	 * outputType = 1 : NER
	 * @param content
	 * @param mysqlIp
	 * @param type
	 * @return
	 */
	
	public EntityLinking(String mysqlIp, String dbUser, String dbPassword){
		mysql = new Mysql("newsProject", mysqlIp, dbUser, dbPassword);
	}
	public ArrayList FillLinks(String Content, int inputType, int outputType)
	{
		//if(inputType == 0)
		//	Content = ANSJsegmentSeg(Content);
		String content = Content.replaceAll("''", "\"");
		
		HashMap<String, Integer> candiId = new HashMap<String, Integer>();
		HashMap<String, Integer> EntityMap = new HashMap<String, Integer>(); 
		GetEntities(EntityMap, content, inputType);
		ArrayList entityList = null; 
		try {
			String entityString = "";
			Iterator<Entry<String, Integer>> it = EntityMap.entrySet().iterator();
			while(it.hasNext())
			{
				Entry<String, Integer> next = it.next();
				if(entityString.equals(""))
					entityString += "'" + next.getKey() + "'";
				else {
					entityString += ", '" + next.getKey() + "'";
					//break;
				}
			}
			entityString = "(" + entityString + ")";
			String sql = "select keyword, entityList, FreqList, id"
					+ " from entity where keyword in ";
			sql += entityString;
			//System.out.println(entityString);
			//System.out.println(sql);
			Statement statm = mysql.conn.createStatement();
			try{
				ResultSet resultS = statm.executeQuery(sql);
				entityList = AddElement(candiId, resultS);
			} catch(Exception e) {
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String result = "";
		String regex = "</wi> ";
		if(inputType == 0)
			regex = " ";
		for(String word : content.split(regex))
		{
			String PlainWord = word;
			//System.out.println(PlainWord);
			if(inputType == 1)
				PlainWord = word.substring(word.indexOf(">") + 1);
			else {
				if (word.indexOf("/")!=-1)
					PlainWord = word.substring(0,word.indexOf("/"));
				else{
					result += word;
					continue;
				}
			}
			
			int pageId = 0;
			if(inputType == 1)
			{
				Matcher m = tag.matcher(word);
				if(m.find())
				{
					if(m.group(2).endsWith("\"O\"") == false && m.group(2).endsWith("\"MISC\"") == false)
					{
						if(candiId.containsKey(PlainWord))
						{
							pageId = candiId.get(PlainWord);
						}
					}
				}
			}
			else if(inputType == 0)
			{
				if(!word.substring(word.indexOf("/")+1).equals("O") && !word.substring(word.indexOf("/")+1).equals("MISC") && candiId.containsKey(PlainWord))
				{
					pageId = candiId.get(PlainWord);
				}
			}
			if(pageId > 0 && word.indexOf("\n")==-1){
				String info = null;
				
				if(outputType == 0)
					info = "<a href=\"http://zh.wikipedia.org/wiki?curid="
						+ pageId + "\">" + PlainWord + "</a>";
				else if(outputType == 1)
					info = word.substring(0, word.indexOf(">")) + " zhwikiURL=\"" +
							"http://zh.wikipedia.org/wiki?curid=" + pageId + "\">" +
							PlainWord + "</wi>";
				else if (outputType==2)
					info = word + "/" + "url=\"http://zh.wikipedia.org/wiki?curid=" + pageId + "\"\n";
				
				result += info;
				
			}
			else{
				if(outputType == 0)
					result += PlainWord;
				else if(outputType == 1)
					result += word.substring(0, word.indexOf(">")) + " zhwikiURL=\"" +
							"\">" + PlainWord + "</wi>";
				else if (outputType==2)
					result += word + " ";
			}
		}
		ArrayList res = new ArrayList();
		res.add(result);
		res.add(entityList);
		return res;
	}
	private void GetEntities(HashMap<String, Integer> entityMap,
			String content, int inputType) {
		// TODO Auto-generated method stub
		String regex = "</wi> ";
		if(inputType == 0)
			regex = " ";
		for(String word : content.split(regex))
		{
			Matcher m = tag.matcher(word);
			String PlainWord;
			
			if (inputType==1)
				PlainWord = word.substring(word.indexOf(">") + 1);
			else {
				word = word.replaceAll("/O", "");
				word = word.replaceAll("/MISC", "");
				if (word.indexOf("/")!=-1)
					PlainWord = word.substring(0,word.indexOf("/"));
				else{
					continue;
				}
			}
				
			while(inputType == 1 && m.find())
			{
				if((m.group(2).endsWith("\"O\"") == false) && (m.group(2).endsWith("\"MISC\"") == false))
				{
					entityMap.put(PlainWord.replaceAll("'", "\\\\'"), 0);
				}
			}
			if(inputType == 0) {
				// System.out.println(PlainWord);
				entityMap.put(PlainWord.replaceAll("'", "\\\\'"), 0);
			}
		}
		
	}
	private ArrayList AddElement(HashMap<String, Integer> candiId, ResultSet result) {
		// TODO Auto-generated method stub
		ArrayList entityList = new ArrayList(); 
		try {
			while(result != null && result.next())
			{
				String []entities = result.getString(2).split(",");
				String []freqs = result.getString(3).split(",");
				entityList.add(result.getInt(4));
				int max = 0;
				// System.out.println(PlainWord);
				int maxEntity = 0;
				for(int i = 0 ; i < entities.length; i ++)
				{
					int freq = Integer.parseInt(freqs[i]);
					if(freq > max)
					{
						max = freq;
						maxEntity = Integer.parseInt(entities[i]);
					}
				}
				candiId.put(result.getString(1), maxEntity);
			}
		} catch (NumberFormatException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entityList;
	}

	private static String ANSJsegmentSeg(String content) {
		// TODO Auto-generated method stub		
		content = content.replaceAll("<[^>]{1,}>", "").replaceAll(" ", "");
		List<Term> seg = NlpAnalysis.parse(content);
		String oneOut = "";
		for(Term term : seg)
		{
			oneOut += term.getName() + " ";
		}
		return oneOut;
	}
	

	private static BufferedReader getBufferedReader(String path) {
		try{
			File file = new File(path);
			if(file.exists() == false){
				System.out.println("read file not exist:"+
						path);
				return null;
			}
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), "utf8"));
			return reader;
		}catch( Exception e){
			e.printStackTrace();
			return null;
		}
	}
	private static void addFile(String string, String path) {
		if(string == null || string.equals(""))
			return;
		try{
			File file=new File(path);
			if(!file.exists()){
				if(file.createNewFile() == false)
					System.out.println("path not exist: " + path);
			}
			OutputStreamWriter osw=
					new OutputStreamWriter(new FileOutputStream(file,true),"utf-8");
			osw.append(string);
			osw.close();
		}catch(Exception e){
			System.out.println(path);
			e.printStackTrace();
		}		
	}
	private static void deleteFile(String path) {
		
		try{
			File file=new File(path);
			if(!file.exists()){
				System.out.println("file " + file.getName() + " not exist, can't delete!");
			}
			else if (file.isFile() == true){
				String name = file.getName();
				file.delete();
				System.out.println("delete " + name + " successfully!");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}
