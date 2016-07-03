import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.sql.*;
import java.util.Map;

/**
 * Created by watergate7 on 2016/4/6.
 */
public class SimpleMysqlPipeline implements Pipeline{
    private final String driver="com.mysql.jdbc.Driver";
    private String dbBase;
    private String dbName;
    private String user;
    private String pw;
    private int batch=0;
    private int maxBatch=0;
    private Connection conn=null;
    private PreparedStatement statement=null;

    SimpleMysqlPipeline(String dbBase,String dbName,String user,String pw,int batch){
        this.dbBase=dbBase;
        this.dbName=dbName;
        this.user=user;
        this.pw=pw;
        this.maxBatch=batch;
        String uri=dbBase+"/"+dbName;

        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(uri, user, pw);
            statement=conn.prepareStatement("insert into nutrient (topCategory,category,name,calory,CHO,fat,protein) values (?,?,?,?,?,?,?)");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void process(ResultItems resultItems, Task task) {
        Map<String,Object> map=resultItems.getAll();
        try {
            String category=map.get("category").toString();
            if(category.endsWith("菜")||category.matches("韩国料理|日本料理|东南亚风味|其他西餐|其他菜肴"))
                statement.setInt(1,2);
            else
                statement.setInt(1,1);
            statement.setString(2, map.get("category").toString());
            statement.setString(3, map.get("name").toString());
            statement.setFloat(4, map.get("calory").toString().equals("一")?(float)0.00:Float.valueOf(map.get("calory").toString()));
            statement.setFloat(5, map.get("CHO").toString().equals("一")?(float)0.00:Float.valueOf(map.get("CHO").toString()));
            statement.setFloat(6, map.get("fat").toString().equals("一")?(float)0.00:Float.valueOf(map.get("fat").toString()));
            statement.setFloat(7, map.get("protein").toString().equals("一")?(float)0.00:Float.valueOf(map.get("protein").toString()));
            statement.addBatch();
            batch++;
            System.out.println(batch);
            if(batch>=maxBatch) {
                statement.executeBatch();
                batch=0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void executeLeftBatch(){
        try {
            if(statement!=null)
                statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
