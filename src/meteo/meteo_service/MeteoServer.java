package meteo.meteo_service;

import constantes.Constantes;
import org.eclipse.paho.client.mqttv3.*;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class MeteoServer {

    public static void main(String[] args) {
        //genera ID RANDOM
        String publisherId = UUID.randomUUID().toString();
        //conecta al broker
        try (Jedis jedis = new Jedis(Constantes.REDIS_SERVER_URI, Constantes.REDIS_SERVER_PORT)) {
            try (MqttClient client = new MqttClient(Constantes.MQTT_SERVER_URI, publisherId)){
                if (!client.isConnected()) {
                    client.connect();
                    //cada vez que llega un mensaje se ejecutar el callback
                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable throwable) {
                            System.out.println("Connection to Solace broker lost! " + throwable.getMessage());
                        }

                        @Override
                        public void messageArrived(String topic, MqttMessage mqttMessage) {
                            //separa el los topics
                            String[] topicSplit = topic.substring(1).split("/");
                            String[] msgSplit = new String(mqttMessage.getPayload()).split("#");
                            //guarda la ultima temperatura en redis
                            String jedisHash = String.format(Constantes.HASH_KEY_LAST_TEMPERATURE_REDIS, Constantes.INICIALES, topicSplit[2]);
                            String jedisList = String.format("%s:TEMPERATURES:%s", Constantes.INICIALES, topicSplit[2]);
                            String jedisListAll = String.format("%s:TEMPERATURES:%s", Constantes.INICIALES, "ALL");
                            String dateTime = String.format("%s-%s", msgSplit[0], msgSplit[1]);
                            //guarda la temperatura en redis y la fecha
                            jedis.hset(jedisHash, "datetime", dateTime);
                            jedis.rpush(jedisListAll, "temperatureAll", msgSplit[2]);
                            jedis.rpush(jedisList, "temperature", msgSplit[2]);
                            //comprueba si la temperatura es extrema
                            float grados = Float.parseFloat(msgSplit[2]);
                            if (grados > 30f || grados < 0f) {
                                //guarda la alerta en redis
                                jedis.set(Constantes.ALERTS_KEY_REDIS, String.format(Constantes.FORMAT_ALERT_STRING, topicSplit));
                                //expira la alerta en 10 segundos
                                jedis.expire(Constantes.ALERTS_KEY_REDIS, 10);
                            }

                        }

                        @Override
                        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        }
                    });
                    //suscribe a todos los topics de las estaciones
                    client.subscribe(Constantes.subscribeAllStationsTopics, 0);
                }

            } catch (MqttException e) {
                e.printStackTrace();
            }

        }
    }
}
