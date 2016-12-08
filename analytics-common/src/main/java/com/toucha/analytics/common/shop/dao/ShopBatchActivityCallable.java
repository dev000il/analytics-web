package com.toucha.analytics.common.shop.dao;

import java.util.List;
import java.util.concurrent.Callable;

/*import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;*/

import com.google.common.collect.Lists;

public class ShopBatchActivityCallable /*implements Callable<List<String[]>>*/ {

    String tablename;
//    List<Get> rks;
    List<String> selectFields;
//    HConnection connection;

    /*public ShopBatchActivityCallable(String tablename, List<Get> rks, List<String> selectFields, HConnection connection) {
        this.tablename = tablename;
        this.rks = rks;
        this.selectFields = selectFields;
        this.connection = connection;
    }

    @Override
    public List<String[]> call() throws Exception {
        HTableInterface table = connection.getTable(tablename);
        Result[] hResult = table.get(rks);

        // For now, we keep all the results in memory, until we stream the data
        // out.
        List<String[]> result = Lists.newArrayList();
        for (int i = 0; i < hResult.length; i++) {
            String[] row = new String[selectFields.size()];
            // System.out.println(new String(hResult[i].getRow(),"utf-8"));
            for (int j = 0; j < selectFields.size(); j++) {
                String f = selectFields.get(j);
                Cell cell = hResult[i].getColumnLatestCell(Bytes.toBytes(ShopScanlogDao.FAMILY_NAME), Bytes.toBytes(f));
                if (f.equals("at") || f.equals("c") || f.equals("rid") || f.equals("pid") || f.equals("okid")) {
                    row[j] = cell == null ? null : String.valueOf(Bytes.toInt(CellUtil.cloneValue(cell)));
                } else {
                    row[j] = cell == null ? null : Bytes.toString(CellUtil.cloneValue(cell));
                }
            }
            result.add(row);
        }
        table.close();
        return result;
    }*/

}
