/**
 * Created by linkedme on 2017/7/18.
 */
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;
import java.io.*;
import java.util.*;

import static com.sun.org.apache.xml.internal.security.keys.keyresolver.KeyResolver.iterator;

public class JedisTest {


    private static JedisTest instance=new JedisTest();
    private static Jedis jedis= null;
    private static final String ipAddr = "192.168.99.116";
//    private static final String ipAddr = "localhost";
    private static final int port = 22222;
    private static Map<String, Integer> hashMap = new HashMap<String, Integer>();
//    private static Map<String, Integer> hashMap;
    private JSONArray json=null;
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


    public void LogtoStringLine(String filename)
    {
        File file = new File(filename);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {

                OneJsontoRedis(getuserapp_id(tempString),getApplisttoString(tempString));
            }

            reader.close();


        }catch (IOException e){
            e.printStackTrace();
        }

    }


    /**
     * 所有的文件内容存为string 字符串，再进行解析每一行（！！不推荐此方法）
     * @param logstring 文件内容
     */
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
        return arr[9];
    }

    /**
     * 每个设备id为key，value为json中取得的app_identifier 的list，存入redis中
     * @param key 用户设备id标识（log日志中第六个字段）
     * @param valuestring  json字符串（log日志中最后一个字段）
     */
    public void OneJsontoRedis(String key,String valuestring)
    {
//        System.out.println(key);
//        System.out.println(valuestring.charAt(0));
        json=JSONArray.fromObject(valuestring);
        if(jedis.exists(key))
        {
            jedis.del(key);
        }
        for(int i= 0;i<json.size();i++)
        {
            JSONObject jsonObject= (JSONObject) json.get(i);
            String app_value=jsonObject.get("app_identifier").toString();
            statisticAppCount(app_value);
            jedis.rpush(key,app_value);
        }
//        System.out.println("userapp_id_key:"+ key);
//        System.out.println(jedis.lrange(key,0,-1));
    }

    /**
     * 统计每个app_identifier对应的key 出现的次数
     * @param key   app_identifier
     */
    public void statisticAppCount(String key)
    {
        if(hashMap.containsKey(key))
        {
            hashMap.put(key,hashMap.get(key).intValue()+1);
        }

        else
        {
            hashMap.put(key,1);
        }
    }

    public void printAppCount(){
        System.out.println("统计：一共有"+hashMap.size()+"个app应用");
        ArrayList<Map.Entry<String,Integer>> entries= sortMap(hashMap);

        Iterator<Map.Entry<String, Integer>> iter = entries.iterator();

        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            String key = entry.getKey().toString();
            String val = entry.getValue().toString();
            System.out.println("key= "+key+"---"+"count= "+val);
        }

//        //直接遍历hashMap时的方法
//        Iterator iter = hashMap.entrySet().iterator();
//        while (iter.hasNext()) {
//            Map.Entry entry = (Map.Entry) iter.next();
//            String key = entry.getKey().toString();
//            String val = entry.getValue().toString();
//            System.out.println("key= "+key+"---"+"count= "+val);
//        }
    }

    //hashmap<string ,int> 按照int降序排列
    public  ArrayList<Map.Entry<String,Integer>> sortMap(Map map) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
                return obj2.getValue() - obj1.getValue();
            }
        });
        return (ArrayList<Map.Entry<String, Integer>>) entries;
    }



    public static void main(String[] args){

        String filename="/Users/linkedme/Documents/linkedme/applist.log.20170718-15";
        JedisTest jedisTest=JedisTest.getInstance();
        try{


            jedisTest.init();
            jedisTest.LogtoStringLine(filename);
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            jedisTest.printAppCount();
        }
//        String filename="/Users/linkedme/Documents/linkedme/applist.log.20170718-15";

//        String filename = args[0];
//        JedisTest jedisTest=JedisTest.getInstance();
//        jedisTest.init();

//        String content=jedisTest.LogtoString(filename);
//        jedisTest.AllLogtoRedis(content);

//        jedisTest.LogtoStringLine(filename);
//        jedisTest.printAppCount();
    }


}
