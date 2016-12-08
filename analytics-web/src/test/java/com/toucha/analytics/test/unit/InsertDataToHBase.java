/**
 * 
 */
package com.toucha.analytics.test.unit;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * @author user
 *
 */
public class InsertDataToHBase {
    static HConnection connection;
    static Configuration conf;
    static final String TABLE_NAME = "activities";
    static final String FAMILY_NAME = "log";
      
    static {
          if (connection == null) {
              conf = HBaseConfiguration.create();
              conf.set("hbase.zookeeper.quorum", "192.168.8.104");
              conf.set("hbase.master", "192.168.8.104:600000");
              
              try {
                  connection = HConnectionManager.createConnection(conf);
              } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
              }
          }
      }  
      
     @SuppressWarnings("deprecation")
    public static void main(String args[]) throws IOException, InterruptedException, ParseException{
         
         HTableInterface hTable;
         
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
         Date ts = dateFormat.parse("2015040100");
        
         hTable = connection.getTable(TABLE_NAME);
         Random rand = new Random();
          for(int i=1; i<=20;i++){
              
              int[] rewardType = new int[2];
              rewardType[0] = 1;
              rewardType[1] = 4;
              
              for(int j=0; j<24; j++) {
              ts.setHours(j);
              String r = RandomStringUtils.randomAlphanumeric(10);
              String rowkey = padding(ts.getTime()) + r;
              Put put = new Put(Bytes.toBytes(rowkey));
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("at"), Bytes.toBytes(2+""));
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("c"), Bytes.toBytes(20+""));
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pid"), Bytes.toBytes(1+""));
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("ts"), Bytes.toBytes(ts.getTime()+""));
              
              /*
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("rt"), Bytes.toBytes(rewardType[rand.nextInt(2)]+""));
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("ra"), Bytes.toBytes(rand.nextInt(110)+""));
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("okid"), Bytes.toBytes(1+""));
              */
              
              
              
              String ptsJson = "{\"ABC\":"+rand.nextInt(20)+",\"DEF\":"+rand.nextInt(20)+"}";
              put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pts"), Bytes.toBytes(ptsJson));
              
              hTable.put(put);
              }
              
              ts.setDate(ts.getDate()+1);
          }
          
           hTable.close();
           connection.close();
          
     }
     
     
     
      public static String padding(Number n){
          String s=null;
          int length = 8;
          if(n instanceof Long){
              s = Long.toHexString((long)n);
              length = 16;
          }else if(n instanceof Integer){
              s = Integer.toHexString((int)n);
          }
          StringBuilder sb =  new StringBuilder();
          for(int i=s.length();i<length;i++){
              sb.append(0);
          }
          return sb.append(s).toString();
      }
}
