import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import java.sql.Statement;
import java.util.ArrayList;

public class news {
	public String title;
	public String URL;
	public int news_id = -1;
	public String source;
	public Timestamp pubtime;
	public int topic_id;
	public String content;
	public String contentWithURL;
	private ArrayList entityList;
	public String outer_id; 	//source_id
	public String category;
	public news(String title, String URL, String source, Timestamp pubtime, int topic, String content, String contentWithURL, ArrayList entityList, String outer_id, String category){
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
	}
	public String mysqlFilter(String s){
		String res = s.replaceAll( "\\\\","\\\\\\\\");
		res = res.replaceAll("'","\\\\'");
		res = res.replaceAll("\"","\\\\\"");
		res = res.replaceAll("%", "\\\\%");
		res = res.replaceAll("_", "\\\\_");
		return res;
	}
	public int save(Statement statm) throws SQLException{
		String sql = "insert into news (title, URL, source, pubtime, topic_id, content, content_with_url, outer_id," +
                " category) values ('" + mysqlFilter(this.title) + "','" + this.URL + "','" + this.source + "','" +
                pubtime + "'," + topic_id + ",'" + mysqlFilter(content)+ "','" + mysqlFilter(contentWithURL) + "','" +
                mysqlFilter(outer_id) + "','" + mysqlFilter(category) + "')";
		System.out.println(this.outer_id);
		try{
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
		return this.news_id;
	}
}
