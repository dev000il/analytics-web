/**
 * 
 */
package com.toucha.analytics.common.dao.test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.lang3.tuple.Pair;
import org.testng.annotations.Test;

/**
 * @author user
 *
 */
public class AnyTest {
    
    @SuppressWarnings("deprecation")
    @Test
    public void testabc() {
        Date d = new Date();
        d.setMonth(4);
        d.setDate(16);
        d.setHours(0);
        d.setMinutes(0);
        d.setSeconds(0);
        
        System.out.println(d);
        System.out.println(d.getTime());
        
        System.out.println(padding(d.getTime()));
    }
    
    @Test
    public void test5() {
    	Date d = new Date(1434349355022l); 
    	System.out.println(d);
    }
    
    @Test
    public void test6() {
    	char[] title = {'\u4EA7','\u54C1','\u8BA2','\u8D2D'};
    	System.out.println(title);
    }
    
    private String padding(Number n) {
        String s = null;
        int length = 8;
        if (n instanceof Long) {
            s = Long.toHexString((long) n);
            length = 16;
        } else if (n instanceof Integer) {
            s = Integer.toHexString((int) n);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = s.length(); i < length; i++) {
            sb.append(0);
        }
        return sb.append(s).toString();
    }
    
    @Test
    private void test2() throws FileNotFoundException, UnsupportedEncodingException {
        
        PrintWriter writer = new PrintWriter("D:\\"+Calendar.getInstance().getTimeInMillis()+".txt", "UTF-8");

        
        
        Map<Pair<Integer, Integer>,Integer> lotteryMaps = new TreeMap<Pair<Integer, Integer>, Integer>();
        lotteryMaps.put(Pair.of(1, 289999),0);
        lotteryMaps.put(Pair.of(289999 + 1, 289999 + 29999), 0);
        lotteryMaps.put(Pair.of(289999 + 29999 + 1, 289999 + 29999 + 10000), 0);
        
        for (int i = 0; i < 100000000; i++) {
            Random r = new Random();
            int nextHit = r.nextInt(1000000);
            for(Pair<Integer, Integer> key: lotteryMaps.keySet()) {
                if (nextHit >= key.getLeft() && nextHit <= key.getRight()) {
                    int counts = lotteryMaps.get(key);
                    lotteryMaps.put(key, ++counts);
                    break;
                }
            }
        }
        
        for (Pair<Integer, Integer> key : lotteryMaps.keySet()) {
            System.out.println(key+":"+lotteryMaps.get(key)/(double)100000000);
            writer.println(key+":"+lotteryMaps.get(key)+"\n");
        }
        
        writer.flush();
        writer.close();
    }
    
    @Test
    private void test3() {
        int range = 3199998 + 1;
        System.out.println(range);
        int first = new BigDecimal(1000000).multiply(new BigDecimal(0.01)).intValue();
        System.out.println(first);
    }
}
