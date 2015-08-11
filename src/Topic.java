/*----------------------------------------------------------------
 *  Author:        Tim Wang
 *  Written:       Oct.25th 2014
 *  Last updated:  Oct.25th 2014
 *
 *  Compilation:   javac Topic.java
 *  Execution:     java Topic
 *  
 *  Get in a file, input the entity "topic" into database and provide method to find the topic of each news pieces. 
 *----------------------------------------------------------------*/
 
import java.util.Scanner;
import java.util.TreeMap;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;  
import org.w3c.dom.Element;  
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
 
 public class Topic			//main class to read and get the topic from a text, store them into the mysql and provide method to get the topic_id for news
 {
 	private TreeMap<String, Integer> news_topic_set; 	//store the Topic_id for each news as TreeMap<News_name, Topic_id>
 	private Mysql mysql;
	private String file_name;
 	
 	private void input_topic()  throws ParserConfigurationException, SAXException, IOException, SQLException
 	{
	 	// step 1: get dom Parser Factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// step 2:get dom Parser
		DocumentBuilder db = dbf.newDocumentBuilder();
		//System.out.println(folder+"/"+fileName);
		File f = new File(file_name);
	     Document document = db.parse(f);
	     NodeList topicList = document.getElementsByTagName("topic");
	     for (int i = 0 ;i< topicList.getLength() ; i++)
	     {
	            Element element = (Element) topicList.item(i);  
	            String name= element.getElementsByTagName("name").item(0).getFirstChild().getNodeValue();
	            String hotWord = element.getElementsByTagName("hotWord").item(0).getFirstChild().getNodeValue();
	            NodeList newsList = element.getElementsByTagName("title");
            	int t_id = 0;
	            String search_topic = "SELECT id FROM topic WHERE name = '"+name+"'";
            	Statement stmt = mysql.conn.createStatement();
            	ResultSet rs;
            	rs = stmt.executeQuery(search_topic);
            	//if (rs.next())
            		//System.out.println(rs.getInt("id"));
            	//System.out.println(rs.next());
	            if (rs.next())
	            	t_id = rs.getInt("id");
	            else 
	            {
	            	String sentence = "INSERT INTO topic (name, hot_words) VALUES('"+name+"', '"+hotWord+"');";	            	
	            	java.sql.PreparedStatement pstmt = mysql.conn.prepareStatement(sentence, Statement.RETURN_GENERATED_KEYS);
	            	pstmt.executeUpdate();
	            	rs = pstmt.getGeneratedKeys();
	            	if(rs!=null && rs.next())  
	            		t_id = rs.getInt(1);	//return the key id of the topic
	            }

	            for (int j = 0 ;j< newsList.getLength() ; j++)
	            {
	            	Element news_element = (Element) newsList.item(j);
	            	String title =  news_element.getFirstChild().getNodeValue();
	            	news_topic_set.put(title,  t_id);
	            }
	      }
 	}
 	
 	public Topic(Mysql sql, String file_name)
 	{
 		mysql = sql;
		this.file_name = file_name;
 		news_topic_set = new TreeMap<String, Integer>();
 		try{
 			input_topic();
 		} catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	public int get_topic(String news_name)	//provide methods to find the topic of the news
 	{
 		news_name = news_name.replaceAll("\n", "");
 		if (news_topic_set.get(news_name) != null)
 			return news_topic_set.get(news_name);
 		else
 			return -1;
 	}
 	
 	public static void main(String[] args)	//required args input as {dbName, IP, dbUser, dbPassword, file_folder, file_name}
 	{
 		Mysql sql = new Mysql(args[0], args[1], args[2], null);
 		Topic new_topic = new Topic(sql, args[4]);
 		Scanner test = new Scanner(System.in);	//testing sentence
 		while (test.hasNext())	//testing sentences
 			System.out.println(new_topic.get_topic(test.next()));
 		sql.disconnectToMysql();
 	}
 }