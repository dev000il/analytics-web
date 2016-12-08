package com.toucha.analytics.common.dao;

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

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.toucha.analytics.common.common.ApplicationConfig;
import com.toucha.analytics.common.model.Mgmevent;

public class ShoplogDao {

   /* static HConnection connection;

    static Configuration conf;*/

    static final String TABLE_NAME_2 = "act_ja2";

    static final String TABLE_NAME_3 = "act_ja3";

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

    @SuppressWarnings("deprecation")
    public DataSet findTopScanlogs(int companyid, List<Integer> dids, List<String> oids, List<Integer> promotions, Date startTime,
            Date endTime, List<String> selectFields, Integer topn, int type) {
        /*endTime = new Date(endTime.getTime() + (59 * 60 + 59) * 1000);

        topn = (topn < 1 || topn > total_size) ? total_size : topn;

        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("ts").from(format.format(startTime)).to(format.format(endTime)))
                .must(QueryBuilders.matchQuery("c", companyid)).must(QueryBuilders.matchQuery("at", type));

        if (dids != null && dids.size() > 0) {
            queryBuilder = queryBuilder.must(QueryBuilders.inQuery("did", dids));
        }
        if (oids != null && oids.size() > 0) {
            queryBuilder = queryBuilder.must(QueryBuilders.inQuery("oid", oids));
        }
        if (promotions != null && promotions.size() > 0) {
            queryBuilder = queryBuilder.must(QueryBuilders.inQuery("promotion", promotions));
        }

        SearchResponse response = null;
        try {
            response = client.prepareSearch("shopindex").setTypes("shop").addSort("ts", SortOrder.DESC).setSize(topn)
                    .setQuery(queryBuilder).execute().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        SearchHit[] hits = response.getHits().getHits();
        System.out.println(hits.length);
        List<Get> rks = new ArrayList<Get>();
        for (int i = 0; i < hits.length; i++) {
            rks.add(new Get(Bytes.toBytes(hits[i].getSource().get("rk").toString())));
        }

        List<String[]> result = new ArrayList<String[]>();
        if (type == 2) {
            result = queryHbase("act_ja2", rks, selectFields, result);
        } else if (type == 3) {
            result = queryHbase("act_ja3", rks, selectFields, result);
        }
        return new DataSet(selectFields, result);*/
        return new DataSet(selectFields, null);
    }

   /* public List<String[]> queryHbase(String tablename, List<Get> rks, List<String> selectFields, List<String[]> result) {
        ThreadFactory factory = new ThreadFactoryBuilder().build();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(rks.size() / batch_size + 1, factory);
        List<Future<List<String[]>>> futures = new ArrayList<Future<List<String[]>>>();
        List<List<Get>> partitionedKeys = Lists.partition(rks, batch_size);
        for (List<Get> s_rks : partitionedKeys) {
            Callable<List<String[]>> callable = new BatchShopCallable(tablename, s_rks, selectFields, connection);
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
