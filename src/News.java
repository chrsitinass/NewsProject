import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class News {
    public String title;
    public String URL;
    public int news_id = -1;
    public String source;
    public Timestamp pubtime;
    public int topic_id;
    public String content;
    public String contentWithURL;
    private ArrayList entityList;
    public String outer_id;
    public String category;
    private HashMap<String, String> map;

    public News(String title, String URL, String source, Timestamp pubtime, int topic, String content,
                String contentWithURL, ArrayList entityList, String outer_id, String category){
        this.title = title;
        this.URL = URL;
        this.source = source;
        this.pubtime = pubtime;
        this.topic_id = topic;
        this.content = content;
        this.contentWithURL = contentWithURL;
        this.entityList = entityList;
        this.outer_id = outer_id;
        this.category = category;
        this.map = new HashMap<String, String>();
        Integer[] num = {4, 2, 3, 4, 3, 4, 5, 6, 3, 3, 2, 1, 1};
        String[] c = {"财经", "科技", "热点", "国际", "军事", "社会", "国内", "体育", "港澳台", "教育", "娱乐", "旅游", "法治"};
        String[][] cate = {{"财经新闻", "经济新闻", "头条新闻-网易财经", "财经频道\\_新华网"},
                {"网易科技头条", "科技新闻"},
                {"实时要闻", "网易头条新闻", "新闻热点"},
                {"国际\\_资讯频道\\_凤凰网", "国际新闻", "网易国际新闻", "国际要闻"},
                {"网易军事新闻", "军事频道\\_凤凰网", "军事新闻"},
                {"社会\\_资讯频道\\_凤凰网", "社会新闻", "社会频道\\_新华网", "网易社会新闻"},
                {"国内新闻", "国内要闻", "网易国内新闻", "大陆\\_资讯频道\\_凤凰网", "地方新闻"},
                {"体育频道\\_凤凰网", "体育", "体育频道\\_新华网", "体育新闻", "综合体育-网易体育频道", "体育新闻"},
                {"港澳台新闻", "台湾新闻", "台湾\\_资讯频道\\_凤凰网", "港澳台"},
                {"网易教育新闻资讯", "教育新闻", "留学频道\\_新华网"},
                {"网易娱乐频道", "娱乐新闻"},
                {"旅游热点\\_网易旅游频道"},
                {"法治频道\\_新华网"}};
        for (int i = 0; i < 13; i += 1) {
            for (int j = 0; j < num[i]; j += 1) {
                map.put(cate[i][j], c[i]);
            }
        }
    }
    public String mysqlFilter(String s){
        String res = s.replaceAll( "\\\\","\\\\\\\\");
        res = res.replaceAll("'", "\\\\'");
        res = res.replaceAll("\"","\\\\\"");
        res = res.replaceAll("%", "\\\\%");
        res = res.replaceAll("_", "\\\\_");
        return res;
    }
    public String trans(String category) {
        String res = "";
        if (map.containsKey(category)) return map.get(category);
        else return "其他";
    }
    public int save(Statement statm) throws SQLException{
        category = category.trim();
        String sql = "insert into temp_news (title, URL, source, pubtime, topic_id, content, content_with_url, outer_id," +
                " category, cate) values ('" + mysqlFilter(this.title) + "','" + this.URL + "','" + this.source + "','" +
                pubtime + "'," + topic_id + ",'" + mysqlFilter(content)+ "','" + mysqlFilter(contentWithURL) + "','" +
                outer_id + "','" + category + "','" + trans(category) + "')";
        System.out.println(this.outer_id);
        try {
            statm.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        }
        catch (Exception e) {
            return -1;
        }
        ResultSet rs = statm.getGeneratedKeys();
        if (rs.next()){
            this.news_id = rs.getInt(1);
        }
        else return -1;
		/*
		for (int i = 0; i < entityList.size(); i += 1){
			sql = "insert into entity_news_history (entity_id, news_id) values (" +
					(int)entityList.get(i) + "," + this.news_id + ")";
			try {
				statm.executeUpdate(sql);
			}
			catch(Exception e) {
				continue;
			}
			sql = "insert into entity_news (entity_id, news_id) values (" +
                    (int)entityList.get(i) + "," + this.news_id + ")";
			try {
				statm.executeUpdate(sql);
			}
			catch (Exception e) {
				continue;
			}
		}
		*/
        return this.news_id;
    }
}
