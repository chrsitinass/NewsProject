import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Mysql {
	public Connection conn;
	public PreparedStatement Query = null;
	public PreparedStatement pstmt ;
	private String dbName, IP, dbUser, dbPassword;

	public Mysql() {
		conn = null;
		Query = null;
	}

	public Mysql(String dbName, String IP, String dbUser, String dbPassword) {
		this.dbName = dbName;
		this.IP = IP;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
		Connect2DB();
	}

	public Connection Connect2DB() {
		// TODO Auto-generated method stub
		try {
			// 调用Class.forName()方法加载驱动程序
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("成功加载MySQL驱动！");
		} catch (ClassNotFoundException e1) {
			System.out.println("找不到MySQL驱动!");
			e1.printStackTrace();
		}
		if (IP == null)
			IP = "localhost";
		// JDBC的URL
		String url = "jdbc:mysql://" + IP + ":3306/" + dbName
				+ "?useUnicode=true&characterEncoding=utf8";
		// 调用DriverManager对象的getConnection()方法，获得一个Connection对象
		try {
			conn = DriverManager.getConnection(url, dbUser, dbPassword);
			// 创建一个Statement对象
			System.out.println("成功连接到数据库！");
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnectToMysql() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 传入连接来执行 SQL 脚本文件，这样可与其外的数据库操作同处一个事物中
	 * 
	 * @param conn
	 *            传入数据库连接
	 * @param sqlFile
	 *            SQL 脚本文件
	 * @throws Exception
	 */
	public void execute(String sqlFile) {
		Statement stmt = null;
		List<String> sqlList;
		try {
			sqlList = loadSql(sqlFile);
			stmt = conn.createStatement();
			for (String sql : sqlList) {
				stmt.addBatch(sql);
			}
			int[] rows = stmt.executeBatch();
			System.out.println("Row count:" + Arrays.toString(rows));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 读取 SQL 文件，获取 SQL 语句
	 * 
	 * @param sqlFile
	 *            SQL 脚本文件
	 * @return List<sql> 返回所有 SQL 语句的 List
	 * @throws Exception
	 */
	private List<String> loadSql(String sqlFile) throws Exception {
		List<String> sqlList = new ArrayList<String>();

		try {
			InputStream sqlFileIn = new FileInputStream(sqlFile);

			StringBuffer sqlSb = new StringBuffer();
			byte[] buff = new byte[1024];
			int byteRead = 0;
			while ((byteRead = sqlFileIn.read(buff)) != -1) {
				sqlSb.append(new String(buff, 0, byteRead));
			}

			// Windows 下换行是 /r/n, Linux 下是 /n
			String[] sqlArr = sqlSb.toString()
					.split("(;\\s*\\r\\n)|(;\\s*\\n)");
			for (int i = 0; i < sqlArr.length; i++) {
				String sql = sqlArr[i].replaceAll("--.*", "").trim();
				if (!sql.equals("")) {
					sqlList.add(sql);
				}
			}
			sqlFileIn.close();
			return sqlList;
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
	}
}