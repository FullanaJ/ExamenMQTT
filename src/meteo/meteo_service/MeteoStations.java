package meteo.meteo_service;

import constantes.Constantes;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.UUID;

public class MeteoStations extends Thread {
    private String uniqueID = "ID";
    private static int uniqueNumber = 0;

    /**
     * Constructor
     */
    public MeteoStations(){
        uniqueID += uniqueNumber;
        uniqueNumber++;
        setName(uniqueID);
    }

    /**
     * Ejecuta el hilo
     */
    @Override
    public void run() {
        //genera ID RANDOM
        String publisherId = UUID.randomUUID().toString();
        System.out.println("La estación " + getName() + " ha sido iniciada");
        //conecta al broker
        try (MqttClient client = new MqttClient(Constantes.MQTT_SERVER_URI, publisherId)){
            Constantes.connectMqttClient(client);
            System.out.println("La estación " + getName() + " se ha conectado al broker");
            while (true) {
                //publica en el topic
                client.publish(createFormatedTopic(), new MqttMessage(createMqttMsg()));
                sleep(5000);
                //si la estación ha sido detenida, para el hilo
                try (Jedis jedis = new Jedis(Constantes.REDIS_SERVER_URI, Constantes.REDIS_SERVER_PORT)) {
                    System.out.println(getName());

                    if(jedis.get(getName())!=null){
                        System.out.println("La estación " + getName() + " ha sido detenida");
                        break;
                    }
                }
            }
        } catch (MqttException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Genera una temperatura aleatoria entre -10 y 40 grados
     * @return
     */
    public float generateTemp(){
        int min = -10;
        int max = 40;

        return new Random().nextFloat(max - min) + min;
    }


    /**
     * Genera un mensaje con la fecha, hora y temperatura
     * @return
     */
    public byte[] createMqttMsg(){
        String msg = String.format("%s#%s#%s", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                String.format("%.2f", generateTemp()));
        System.out.println(msg);
        return msg.getBytes();
    }

    /**
     * Devuelve el MqqttTopic con el formato pedido
     * @return
     */
    public String createFormatedTopic(){
        String topic = String.format("/%s/METEO/", Constantes.INICIALES) + uniqueID + "/MEASUREMENTS";

        return topic;
    }
}
