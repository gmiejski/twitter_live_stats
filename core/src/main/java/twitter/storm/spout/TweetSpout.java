package twitter.storm.spout;

import backtype.storm.Config;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import twitter.Twitter4jReader;
import twitter.rest.KeywordsRepository;
import twitter4j.Status;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by grzmiejski on 4/18/15.
 */
public class TweetSpout extends BaseRichSpout {

    private SpoutOutputCollector spoutOutputCollector;
    private Queue<Status> queue;
    private Twitter4jReader twitter4jReader;
    private List<String> keywords;

    public TweetSpout(List<String> keywords) {
        this.keywords = keywords;
    }

    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
        queue = new LinkedBlockingQueue<>(1000);
        this.twitter4jReader = new Twitter4jReader(queue, keywords, new KeywordsRepository());
        twitter4jReader.startStreaming();
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("tweet"));
    }

    public void nextTuple() {
        Status status = queue.poll();
        if (status == null) {
            Utils.sleep(50);
        } else {
//            if (status.getText().toLowerCase().contains("sun") && status.getText().toLowerCase().contains("spring")) {
//                System.out.println("contains sun && spring");
//                System.out.println(status.getText().toLowerCase());
//            } else if (status.getText().toLowerCase().contains("sun")) {
//                System.out.println("contains sun ");
//                System.out.println(status.getText().toLowerCase());
//            } else {
//                System.out.println("contains spring");
//                System.out.println(status.getText().toLowerCase());
//            }
            spoutOutputCollector.emit(new Values(status));
        }
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        Config ret = new Config();
        ret.setMaxTaskParallelism(1);
        return ret;
    }

    @Override
    public void close() {
        twitter4jReader.shutdown();
    }
}
