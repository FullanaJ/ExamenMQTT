package constantes;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class Constantes {
    public static final String MQTT_SERVER_URI = "tcp://localhost";
    public static final String REDIS_SERVER_URI = "localhost";
    public static final int REDIS_SERVER_PORT = 6379;
    public static final String INICIALES = "JFE";
    public static final String subscribeAllStationsTopics = "/JFE/METEO/#";
    public static final String ALERTS_KEY_REDIS = "JFE:ALERTS";
    public static final String FORMAT_ALERT_STRING = "Alerta por temperaturas extremas el %s a las %s en la estación %s";
    public static final String HASH_KEY_LAST_TEMPERATURE_REDIS = "%s:LASTMEASUREMENT:%s";

    public static void connectMqttClient(MqttClient client) throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        client.connect(options);
    }
}
