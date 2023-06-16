package com.example.uas_pemrogiot_4170;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MqttCallback {

    MqttClient client = null;

    ArrayList<LineDataSet> flowDataSet;

    LineChart bc;
    LineChart flowLC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectToMQTTBroker();

        Log.d("MQTT", "subscribed");

        bc = findViewById(R.id.myChart);
        flowLC = findViewById(R.id.soilChart);

        LineData data = new LineData(getLabel(10), getDummyDataSet());
        bc.setData(data);
        bc.animateXY(4000, 4000);
        bc.invalidate();

        LineData flowData = new LineData(getLabel(10));
        flowLC.setData(flowData);
        flowLC.animateXY(4000, 4000);
        flowLC.invalidate();
        /* ini udh ada flowData, asumsikan klo mau update data, yang diupdate yg flowData terus
        flowLC.notifyDataChanged();
        flowLC.notifyDataChanged();

        LineData data = lineChart.getData();
        ILineDataSet set = data.getDataSetByIndex(0);
        data.addEntry(new Entry(set.getEntryCount(),iv),0);
        data.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMaximum(10);
        lineChart.moveViewToX(data.getEntryCount());
         */
    }

    private void connectToMQTTBroker() {
        try {
            client = new MqttClient("tcp://192.168.41.70:1883", "asdasd", new MemoryPersistence());
            client.setCallback(this);
            client.connect();

            // Re-subscribe to topics after reconnection if needed
            client.subscribe("4170/flow");
            client.subscribe("4170/soil");
            client.subscribe("4170/ldr");
            client.subscribe("4170/dht11/temp");
            client.subscribe("4170/dht11/hum");

            Log.d("MQTT", "Connected to MQTT broker");
        } catch (MqttException e) {
            e.printStackTrace();
            Log.d("MQTT", "Connection to MQTT broker failed");
        }
    }

    private ArrayList getLabel(int n) {
        ArrayList xLabel = new ArrayList();
        for(int i = 0; i < n; i++) {
            xLabel.add("n: " + Integer.toString(i + 1));
        }
        return xLabel;
    }

    private ArrayList getDummyDataSet() {
        ArrayList dataset = null; //buat inisialisasi

        ArrayList valueset1 = new ArrayList();
        ArrayList valueset2 = new ArrayList();

        int val;
        for (int i = 0; i < 10; i++) {
            val = (int) (Math.random() * 200 - 30);
            valueset1.add(new Entry(val, i));
            val = (int) (Math.random() * 200 - 30);
            valueset2.add(new Entry(val, i));
        }

        LineDataSet bds1 = new LineDataSet(valueset1, "Data Set 1");
        LineDataSet bds2 = new LineDataSet(valueset2, "Data Set 2");
        bds1.setColor(Color.rgb(0, 200, 0));
        bds2.setColor(Color.rgb(0, 0, 150));

        dataset = new ArrayList();
        dataset.add(bds1);
        dataset.add(bds2);
        return dataset;
    }

    /*
    private ArrayList getSensorDataSet(float val, LineData sensorData) {
        ArrayList dataset = null;

        if (sensorData == null) {
            ArrayList valueset = new ArrayList();

            valueset.add(new Entry(val, 0));

            LineDataSet dataSet = new LineDataSet(valueset, "Data Set");
            dataSet.setColor(Color.rgb(0, 200, 0));

            dataset = new ArrayList();
            dataset.add(dataSet);
            Log.d("MQTT", "sensorData == null");
        } else {
            LineDataSet dataSet = (LineDataSet) sensorData.getDataSetByIndex(0);
            Entry newEntry = new Entry(val, dataSet.getEntryCount());
            dataSet.addEntry(newEntry);
            sensorData.notifyDataChanged();
            Log.d("MQTT", "sensorData != null");
        }
        return dataset;
    }
*/
    @Override
    public void connectionLost(Throwable cause) {
        Log.d("MQTT", "Connection lost: " + cause.getMessage());
        // Handle reconnection logic here
        connectToMQTTBroker();
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        Log.d("MQTT", "topic: " + topic);
        Log.d("MQTT", "message: " + message);
        float value = Float.parseFloat(String.valueOf(message));

        if (topic.equals("4170/flow")) {
            // Update flow line chart with the received data
            // ...
            updateFlowLineChart(value);

        } else if (topic.equals("4170/soil")) {
            // Update soil line chart with the received data
            // ...

        } else if (topic.equals("4170/ldr")) {
            // Update ldr line chart with the received data
            // ...

        } else if (topic.equals("4170/dht11/temp")) {
            // Update temperature line chart with the received data
            // ...

        } else if (topic.equals("4170/dht11/hum")) {
            // Update humidity line chart with the received data
            // ...
        }
    }

    private void updateFlowLineChart(float value) {
        LineData lineData = flowLC.getData();

        if (lineData != null) {
            ILineDataSet dataSet = lineData.getDataSetByIndex(0);

            if (dataSet == null) {
                dataSet = createDataSet();
                lineData.addDataSet(dataSet);
            }

            int entryCount = dataSet.getEntryCount();

            if (entryCount >= 10) {
                //dataSet.removeEntry(0);
                for (int i = 0; i < 10; i++) {
                    dataSet.removeEntry(i);
                }

                Entry newEntry = new Entry(value, 0);
                dataSet.addEntry(newEntry);
            } else {
                Entry newEntry = new Entry(value, entryCount);
                dataSet.addEntry(newEntry);
            }

            lineData.notifyDataChanged();
            flowLC.notifyDataSetChanged();
            flowLC.setVisibleXRangeMaximum(10);
            flowLC.moveViewToX(-1);
        }
    }

    private LineDataSet createDataSet() {
        LineDataSet dataSet = new LineDataSet(null, "Flow Data");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setDrawValues(false);
        return dataSet;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}