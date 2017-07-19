/**
 * Created by linkedme on 2017/7/18.
 */
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import java.io.*;

public class JedisTest {


    private static JedisTest instance=new JedisTest();
    private static Jedis jedis= null;
    private static final String ipAddr = "192.168.99.116";
    private static final int port = 22222;
    private JedisTest()
    {

    }
    public static JedisTest getInstance()
    {
        return instance;
    }


    public static void init()
    {
        jedis = JedisTest.getInstance().getJedis(ipAddr, port);
    }



    public Jedis getJedis(String ip, int port)
    {
        jedis=new Jedis(ip,port);
        return  jedis;
    }

    public String LogtoString(String fileName)
    {
        String content = "";
        try {
            File file = new File(fileName);
            FileReader reader = new FileReader(file);
            int ch = 0;
            while ((ch = reader.read()) != -1)
            {
                content += (char) ch;
            }
            reader.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    public void jedisTestList(String key, String value)
    {
        OneJsontoRedis(key, value);
    }



    public void AllLogtoRedis(String logstring)
    {
        String [] arr = logstring.split("\n");

        for(int i=0;i<arr.length;i++)
        {
            OneJsontoRedis(getuserapp_id(arr[i]),getApplisttoString(arr[i]));
        }

    }

    public String[] getLogArray(String logstring)
    {
        return logstring.split("\t");
    }

    public String getuserapp_id(String logstring)
    {
        String [] arr=getLogArray(logstring);
        return arr[5];
    }


    public String getApplisttoString(String logstring)
    {
        String [] arr = getLogArray(logstring);
        return arr[8];
    }

    public void OneJsontoRedis(String key,String valuestring)
    {
        JSONArray json=JSONArray.fromObject(valuestring);
        if(jedis.exists(key))
        {
            jedis.del(key);
        }
        for(int i= 0;i<json.size();i++)
        {
            JSONObject jsonObject= (JSONObject) json.get(i);
            jedis.rpush(key,jsonObject.get("app_identifier").toString());
        }

        System.out.println("userapp_id_key:"+ key);
        System.out.println(jedis.lrange(key,0,-1));
    }




    public static void main(String[] args){

        String filename="/Users/linkedme/Documents/applist.log";
        JedisTest jedisTest=JedisTest.getInstance();
        jedisTest.init();

        String content=jedisTest.LogtoString(filename);
        jedisTest.AllLogtoRedis(content);

    }


}
