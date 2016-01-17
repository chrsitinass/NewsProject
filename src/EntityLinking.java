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
	}

	static Pattern tag = Pattern.compile("<wi( ([^ >]{1,}))+>");
	private Mysql mysql;
	/**
	 * @param content
	 * @param mysqlIp
	 * @param type
	 * @return
	 */

	public EntityLinking(String mysqlIp, String dbUser, String dbPassword){
		mysql = new Mysql("newsProject", mysqlIp, dbUser, dbPassword);
	}

	/*
	 * 2016-1-16 update by husen
	 * Notice:
	 * 1. The "rawContent" should be NERed before call this function.
	 * 2. "inputType" and "outputType" are unused.
	 * Modify:
	 * 1. Delete useless codes.
	 * 2. Simplify the logic.
	 * 3. Fix some format errors.
	 * */
	public ArrayList FillLinks(String rawContent, int inputType, int outputType)
	{
		ArrayList res = new ArrayList();

		// Fix some format errors.
		String content = rawContent.replaceAll("`", "'");
		content = content.replaceAll("''", "\"");
		content = content.replaceAll("《 ", "《/O ");
		content = content.replaceAll("》 ", "》/O ");

		HashMap<String, Integer> candidateWithEntityId = new HashMap<String, Integer>();
		HashMap<String, Integer> candidateWithOccurCnt = new HashMap<String, Integer>();

		// This "entityList" is unknown, unused and useless now. I want to delete it but rejected by ZengShen.
		ArrayList entityList = null;

		// Get <"NE word", "occur count"> from content. The "occur count" is not used by now, but it may be useful in further.
		candidateWithOccurCnt = GetEntities(content);
		if(candidateWithOccurCnt == null || candidateWithOccurCnt.size() == 0)
			return res;

		try
		{
			String candidatesStr = "";
			Iterator<Entry<String, Integer>> it = candidateWithOccurCnt.entrySet().iterator();
			while(it.hasNext())
			{
				Entry<String, Integer> next = it.next();
				if(candidatesStr.equals(""))
					candidatesStr += "'" + next.getKey() + "'";
				else
					candidatesStr += ", '" + next.getKey() + "'";
			}

			String sql = "select keyword, entityList, FreqList, id"
					+ " from entity where keyword in (" + candidatesStr + ")";
			Statement statm = mysql.conn.createStatement();
			ResultSet resultS = statm.executeQuery(sql);

			entityList = AddElement(candidateWithEntityId, resultS);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		String result = "";
		String delimiter = " ";

		for(String word : content.split(delimiter))
		{
			String PlainWord = word;

			if (word.indexOf("/")!=-1)
				PlainWord = word.substring(0,word.indexOf("/"));
			else
			{
				result += word;
				continue;
			}

			int pageId = 0; //An entity's pageId is also its entityId.

			// This word is NE, and we has got its entityId.
			if(!word.substring(word.indexOf("/")+1).equals("O") && !word.substring(word.indexOf("/")+1).equals("MISC") && candidateWithEntityId.containsKey(PlainWord))
			{
				pageId = candidateWithEntityId.get(PlainWord);
			}

			result += word;
			if(pageId > 0 && word.indexOf("\n") == -1)
			{
				String info = "/" + "url=\"http://zh.wikipedia.org/wiki?curid=" + pageId + "\"";
				result += info;
			}

			result += " ";
		}

		res.add(result);
		res.add(entityList);

		return res;
	}

	private HashMap<String, Integer> GetEntities(String content)
	{
		HashMap<String, Integer> entityMap = new HashMap<String, Integer>();

		String delimiter = " ";
		for(String word : content.split(delimiter))
		{
			Matcher m = tag.matcher(word);
			String PlainWord;

			word = word.replaceAll("/O", "");
			word = word.replaceAll("/MISC", "");
			if (word.indexOf("/")!=-1)
				PlainWord = word.substring(0,word.indexOf("/"));
			else
				continue;

			if(!entityMap.containsKey(PlainWord.replaceAll("'", "\\\\'")))
				entityMap.put(PlainWord.replaceAll("'", "\\\\'"), 0);
			else
				entityMap.put(PlainWord.replaceAll("'", "\\\\'"), entityMap.get(PlainWord.replaceAll("'", "\\\\'"))+1);
		}

		return entityMap;
	}

	private ArrayList AddElement(HashMap<String, Integer> candiId, ResultSet result)
	{
		ArrayList entityList = new ArrayList();
		try
		{
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
		}
		catch (NumberFormatException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entityList;
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
}