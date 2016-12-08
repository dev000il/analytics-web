package com.toucha.analytics.common.dao;

public class BatchShopCallable/* implements Callable<List<String[]>>*/ {

   /* String tablename;
    List<Get> rks;
    List<String> selectFields;
    HConnection connection;

    public BatchShopCallable(String tablename, List<Get> rks, List<String> selectFields, HConnection connection) {
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
                Cell cell = hResult[i].getColumnLatestCell(Bytes.toBytes(ShoplogDao.FAMILY_NAME), Bytes.toBytes(f));
                if (f.equals("rid") || f.equals("pid") || f.equals("okid") || f.equals("pri")) {
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
