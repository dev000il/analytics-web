package com.toucha.analytics.common.shop.dao;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.util.Bytes;*/
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.model.Mgmevent;

public class ShopScanlogDao {

//    static HConnection connection;
//
//    static Configuration conf;

    static final String TABLE_NAME_2 = "act_zb2";

    static final String TABLE_NAME_3 = "act_zb3";

    static final String FAMILY_NAME = "lg";

    static Client client;

    static final int batch_size = 1000;

    static final int total_size = 10000;

    static {
        /*if (connection == null) {
            conf = HBaseConfiguration.create();
            conf.set("hbase.zookeeper.quorum", ApplicationConfig.hbaseZookeeperQuorum);
            try {
                connection = HConnectionManager.createConnection(conf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        if (client == null) {
            client = new TransportClient(ImmutableSettings.builder().put("cluster.name", "es").build())
                    .addTransportAddress(new InetSocketTransportAddress(ApplicationConfig.ElasticSearchServer, 9300));
        }
    }

   /**
     * 
     * @param companyId
     * @param productIds
     * @param startTime
     * @param endTime
     * @param selectFields
     * @param topn
     * @return
     * @throws IOException
     *
     */
    @SuppressWarnings("deprecation")
    public ShopDataSet findTopScanlogs(int companyId, List<Integer> productIds, Date startTime, Date endTime,
            List<String> selectFields, Integer topn) throws IOException {

        /*endTime = new Date(endTime.getTime() + (59 * 60 + 59) * 1000);

        System.out.println("request params ==> topn:" + topn + " companyId:" + companyId + " productIds:"
                + Joiner.on(",").join(productIds) + " startTime: " + startTime + " endTime:" + endTime);
        long starttime = new Date().getTime();
        topn = (topn < 1 || topn > total_size) ? total_size : topn;
        Preconditions.checkArgument(selectFields.contains("c"), "Company is required because its used in the query filter.");
        Preconditions.checkArgument(selectFields.contains("at"),
                "Due to hbase's bug filtering fields must appear in the selected fields, here we're checking if activity type equals to 2 or 3");
        if (productIds != null && !productIds.isEmpty()) {
            Preconditions.checkArgument(selectFields.contains("pid"),
                    "Product is required because its used in the query filter.");
        }

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startTime)).to(format.format(endTime)))
                .must(QueryBuilders.matchQuery("c", companyId));
        if (productIds != null && productIds.size() > 0) {
            queryBuilder = queryBuilder.must(QueryBuilders.inQuery("pid", productIds));
        }
        SearchResponse response = null;
        try {
            response = client.prepareSearch("actindex").setTypes("act").addSort("ts", SortOrder.DESC).setSize(topn)
                    .setQuery(queryBuilder).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        SearchHit[] hits = response.getHits().getHits();
        System.out.println(hits.length);
        List<Get> rks2 = new ArrayList<Get>();
        List<Get> rks3 = new ArrayList<Get>();
        for (int i = 0; i < hits.length; i++) {
            if ((int) (hits[i].getSource().get("at")) == 2) {
                rks2.add(new Get(Bytes.toBytes(hits[i].getSource().get("rk").toString())));
            } else {
                rks3.add(new Get(Bytes.toBytes(hits[i].getSource().get("rk").toString())));
            }
        }

        List<String[]> result2 = new ArrayList<String[]>();
        List<String[]> result3 = new ArrayList<String[]>();
        result2 = queryHbase("act_zb2", rks2, selectFields, result2);
        result3 = queryHbase("act_zb3", rks3, selectFields, result3);
        result2.addAll(result3);
        long endtime = new Date().getTime();
        System.out.println("done..." + (endtime - starttime));
        return new ShopDataSet(selectFields, result2);*/
        return new ShopDataSet(selectFields, null);
    }

    /*public List<String[]> queryHbase(String tablename, List<Get> rks, List<String> selectFields, List<String[]> result) {
        ThreadFactory factory = new ThreadFactoryBuilder().build();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(rks.size() / batch_size + 1, factory);
        List<Future<List<String[]>>> futures = new ArrayList<Future<List<String[]>>>();
        List<List<Get>> partitionedKeys = Lists.partition(rks, batch_size);
        for (List<Get> s_rks : partitionedKeys) {
            Callable<List<String[]>> callable = new ShopBatchActivityCallable(tablename, s_rks, selectFields, connection);
            Future<List<String[]>> future = executor.submit(callable);
            futures.add(future);
        }
        executor.shutdown();
        try {
            boolean stillRunning = !executor.awaitTermination(15, TimeUnit.SECONDS);
            if (stillRunning) {
                try {
                    executor.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException e) {
            try {
                Thread.currentThread().interrupt();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        for (Future<List<String[]>> f : futures) {
            try {
                if (f.get() != null) {
                    result.addAll(f.get());
                }
            } catch (InterruptedException e) {
                try {
                    Thread.currentThread().interrupt();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        return result;
    }*/

    public List<Mgmevent> findUserMgmevents(int c, String u, Date startTime, Date endTime) {
        endTime = new Date(endTime.getTime() + (59 * 60 + 59) * 1000);
        System.out.println("request params ==> c:" + c + " u:" + u + " startTime: " + startTime + " endTime:" + endTime);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startTime)).to(format.format(endTime)))
                .must(QueryBuilders.matchQuery("c", c));
        SearchResponse response = null;
        try {
            response = client.prepareSearch("mgmeventsindex").setTypes("mgmevent").addSort("ts", SortOrder.DESC).setSize(10000)
                    .setQuery(queryBuilder).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        SearchHit[] hits = response.getHits().getHits();
        System.out.println(hits.length);
        List<Mgmevent> events = new ArrayList<Mgmevent>();
        for (int i = 0; i < hits.length; i++) {
            Map<String, Object> map = hits[i].getSource();
            try {
                events.add(new Mgmevent(map.get("u").toString(), map.get("a").toString(), format.parse(map.get("ts").toString()),
                        map.get("msg").toString(), map.get("oip").toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return events;
    }
}
