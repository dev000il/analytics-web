package com.toucha.analytics.common.dao.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;

import com.alibaba.fastjson.JSON;

public class LoadDataToHbase {
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
	    
	   public static void main(String args[]) throws IOException, InterruptedException{
		   
		   HTableInterface hTable;
		   List<Put> puts = new ArrayList<Put>();
		  
		   hTable = connection.getTable(TABLE_NAME);
			for(int i=1; i<=100;i++){
				
				String hid = RandomStringUtils.randomAlphanumeric(10);
				
				Random rd = new Random(i);
				int pid = rd.nextInt(3)+1;
				int rt = rd.nextInt(5);
				
				long ts = new Date().getTime();
				String r = RandomStringUtils.randomAlphanumeric(10);
				String pn = RandomStringUtils.randomNumeric(11);
				String rowkey = padding(ts) + r;
				Put put = new Put(Bytes.toBytes(rowkey), ts);
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("at"), Bytes.toBytes("2"));
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("c"), Bytes.toBytes(i+""));
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pid"), Bytes.toBytes(pid+""));
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("hid"), Bytes.toBytes(hid+""));
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("ts"), Bytes.toBytes(ts+""));
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("city"), Bytes.toBytes("上海"));
				
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pn"), Bytes.toBytes(pn+""));
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pri"), Bytes.toBytes(i+""));
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("ptri"), Bytes.toBytes(i+""));
				Map<String,Integer> pts = new HashMap<String,Integer>();
				pts.put("HENGDA", i);
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pts"), Bytes.toBytes(JSON.toJSONString(pts)));
				Map<String,String> rwds = new HashMap<String,String>();
				rwds.put(i+"", i+"");
				put.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("rwds"), Bytes.toBytes(JSON.toJSONString(rwds)));
				
				long ts2 = new Date().getTime();
				String r2 = RandomStringUtils.randomAlphanumeric(10);
				String rowkey2 = padding(ts2) + r2;
				Put put2 = new Put(Bytes.toBytes(rowkey2), ts2);
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("at"), Bytes.toBytes(3+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("c"), Bytes.toBytes(i+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pid"), Bytes.toBytes(pid+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("hid"), Bytes.toBytes(hid+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("ts"), Bytes.toBytes(ts2+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("city"), Bytes.toBytes("上海"));
				
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("pn"), Bytes.toBytes(pn+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("rid"), Bytes.toBytes(i+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("rt"), Bytes.toBytes(rt+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("rn"), Bytes.toBytes(i+""));
				put2.add(Bytes.toBytes(FAMILY_NAME), Bytes.toBytes("okid"), Bytes.toBytes(pid+""));
				
				puts.add(put);
				puts.add(put2);
				
				if(puts.size() %10 ==0){
					hTable.put(puts);
					puts.clear();
					Thread.sleep(1000);
				}
			}
			 hTable.put(puts);
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
