import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.*;

public class readNews {
	private String folder;
	private String fileName;
	private String serializedClassifier;
	private AbstractSequenceClassifier<CoreLabel> classifier;
	private Segmentation Seg;
	private NERDemo_Chinese nerC;
	private EntityLinking EL;
	private Mysql mysql;
	private Statement statm;
	private Topic topic;
	private String category;

	public void saveTopic(Map <entityToTopic,Integer> entity_topic, Statement statm) throws SQLException{
		Iterator <entityToTopic> iter = entity_topic.keySet().iterator();
		while (iter.hasNext()){
			entityToTopic key = iter.next();
			int value = entity_topic.get(key);
			String sql = "insert into entity_topic_history (entity_id, topic_id, count) values (" +
                    key.x + "," + key.y + "," + value + ")";
			try {
				statm.executeUpdate(sql);
			}
			catch (Exception e) {
				continue;
			}
			sql = "insert into entity_topic (entity_id, topic_id, count) values (" +
                    key.x + "," + key.y + "," + value + ")";
			try{
				statm.executeUpdate(sql);
			}
			catch (Exception e) {
				continue;
			}
		}
	}

    public static String getCharacterDataFromElement(Element e) {
        Node child = e.getFirstChild();
        short node_type = 0;
        try {
            node_type = child.getNodeType();
        } catch (Exception exc) {
            return "";
        }
        String res = "";
        if (node_type == Document.TEXT_NODE) {
            res = ((Text)child).getData();
        } else if (node_type == Document.CDATA_SECTION_NODE) {
            res = ((CDATASection)child).getNodeValue();
        }
        return res.trim();
    }

	public void read() throws ParserConfigurationException, SAXException, IOException, SQLException {
		// step 1: 获得dom解析器工厂（工作的作用是用于创建具体的解析器）
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// step 2: 获得具体的dom解析器
        dbf.setCoalescing(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		File f = new File(fileName);
        Document document = db.parse(f);
        NodeList docList = document.getElementsByTagName("item");
        Map <entityToTopic,Integer> entity_topic = new HashMap<entityToTopic,Integer>();

        /* step 3: 解析xml
         * format:
         * <item>
         *     <id>`outer_id`</id>
         *     <link>`URL`</link>
         *     <pubDate>`pubtime`</pubDate>
         *     <category>`category`</category>
         *     <title>`title`</title>
         *     <source>`source`</source>
         *     <content>`content`</content>
         * </item>
         */
        for (int i = 0; i < docList.getLength(); i += 1) {
            Element element = (Element)docList.item(i);
            String outer_id = element.getElementsByTagName("id").item(0).getFirstChild().getNodeValue();
            String URL = element.getElementsByTagName("link").item(0).getFirstChild().getNodeValue();
            Timestamp pubtime = new Timestamp(System.currentTimeMillis());
            String temp_pubtime = element.getElementsByTagName("pubDate").item(0).getFirstChild().getNodeValue();
            pubtime = Timestamp.valueOf(temp_pubtime);
            String category = element.getElementsByTagName("category").item(0).getFirstChild().getNodeValue();
            String source = element.getElementsByTagName("source").item(0).getFirstChild().getNodeValue();

            NodeList title_node_list = element.getElementsByTagName("title");
            NodeList content_node_list = element.getElementsByTagName("content");
            Element title_node = (Element) title_node_list.item(0);
            Element content_node = (Element) content_node_list.item(0);
            String title  = getCharacterDataFromElement(title_node);
            String content = getCharacterDataFromElement(content_node);
            
            // TODO: fix topic modular
            // int topic = this.topic.get_topic(title);
            int topic = -1;

            // label content
            if (content.length() == 0) {
                System.out.println("Empty content");
                continue;
            }
            String NERInput = Seg.getSegArray(content);
            String result = new String();
            try {
    			result = nerC.getStringRes(classifier, NERInput);
    		}
    		catch(Exception e) {
    			System.out.println(e);
    		}
            try {
            	ArrayList res = EL.FillLinks(result, 0, 2);
            	String contentWithUrl = (String)res.get(0);
            	ArrayList entityList = (ArrayList)res.get(1);
            	for (int j = 0; j < entityList.size(); j += 1) {
            		int entity = (int)entityList.get(j);
            		entityToTopic ett = new entityToTopic(entity,topic);
            		if (!entity_topic.containsKey(ett))
            			entity_topic.put(ett, 1);
            		else{
            			int num = entity_topic.get(ett)+1;
            			entity_topic.put(ett, num);
            		}
            	}
            	news basicNews = new news(title, URL, source, pubtime, topic, content, contentWithUrl, entityList,
                        outer_id, category);
            	basicNews.save(statm);
            } catch(Exception e) {}
        }
        // saveTopic(entity_topic, statm);
	}
	public readNews(String folder, String fileName, String ip, String dbUser, String dbPassword)
            throws ClassCastException, ClassNotFoundException, IOException, SQLException {
		this.folder = folder;
		this.fileName = fileName;
		this.serializedClassifier = "classifiers/chinese.misc.distsim.crf.ser.gz";
		this.classifier = CRFClassifier.getClassifier(serializedClassifier);
		this.Seg = new Segmentation();
		this.nerC = new NERDemo_Chinese(); 
		this.EL = new EntityLinking(ip, dbUser, dbPassword);
		this.mysql = new Mysql("newsProject", ip, dbUser, dbPassword);
        this.statm = mysql.conn.createStatement();
		this.topic = new Topic(this.mysql, "data/match.xml");
	}
	public static void main(String[] args) throws Exception {
		String folder = "/media/damon/SinaNews/xmlOut/";
		// String folder = "xmlOut";
        File file = new File(folder);
		File[] files = file.listFiles();
		if (files != null) {
			for (File f: files) {
				String fileName = f.getPath();
				System.out.println(fileName);
				readNews rn = new readNews(folder, fileName, "172.31.19.9", "root", "");
				try {
					rn.read();   
				}
				catch(Exception e) {
					System.out.println(e);
				}
				System.out.println("finish" + fileName);
			}
		}
	}
}