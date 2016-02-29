import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichSpout;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Map;

public class WordReader implements IRichSpout {

    private boolean completed = false;
    private FileReader fileReader;
    private SpoutOutputCollector collector;
    private TopologyContext context;

    public boolean isDistributed() {return false;}

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("line"));

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }

    /**
     * 我们将创建一个文件并维持一个collector对象
     */
    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        try {
            this.context = topologyContext;
            this.fileReader = new FileReader(map.get("wordsFile").toString());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Error reading file [" + map.get("wordFile") + "]");
        }
        this.collector = spoutOutputCollector;

    }

    @Override
    public void close() {

    }

    @Override
    public void activate() {

    }

    @Override
    public void deactivate() {

    }

    /**
     * 这个方法做的惟一一件事情就是分发文件中的文本行
     */
    @Override
    public void nextTuple() {

        /**
         * 这个方法会不断的被调用，直到整个文件都读完了，我们将等待并返回。
         */
        if (completed) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                //什么也不做
            }
            return;
        }

        String str;
        //创建reader
        BufferedReader reader = new BufferedReader(fileReader);
        try {
            //读所有文本行
            while ((str = reader.readLine()) != null) {
                /**
                 * 按行发布一个新值
                 */
                this.collector.emit(new Values(str), str);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading tuple", e);
        } finally {
            completed = true;
        }
    }

    @Override
    public void ack(Object o) {
        System.out.println("OK:" + o);
    }

    @Override
    public void fail(Object o) {
        System.out.println("FAIL:" + o);
    }
}
