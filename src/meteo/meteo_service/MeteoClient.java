package meteo.meteo_service;

import constantes.Constantes;
import redis.clients.jedis.Jedis;

import java.util.Scanner;
import java.util.Set;

public class MeteoClient {
    public static void main(String[] args) {
        String c = "";
        //crea un scanner para leer los comandos
        Scanner sc = new Scanner(System.in);
        //conecta a redis
        try (Jedis jedis = new Jedis(Constantes.REDIS_SERVER_URI, Constantes.REDIS_SERVER_PORT)) {
            //mientras no se introduzca el comando EXIT
            while (!c.equals("EXIT")) {
                //lee el comando
                System.out.print("Introduce algún comando LAST ID,MAXTEMP ID,MAXTEMP ALL,ALERTS, STOP ID : ");
                String comando = sc.nextLine() + " ";
                String[] split = comando.split(" ", 2);
                c = split[0];
                //ejecuta el comando
                switch (c) {
                    case "LAST" -> {
                        //obtiene la ultima temperatura de la estación
                        String lastTemperature = jedis.hget(String.format(Constantes.HASH_KEY_LAST_TEMPERATURE_REDIS, Constantes.INICIALES, split[1]), "temperature");
                        System.out.printf("Esta es la ultima temperatura registrada por la estación %s: %s\n", split[1], lastTemperature);
                    }
                    case "MAXTEMP" -> {
                        try {
                            //obtiene la temperatura
                            if (split[1].equals("ALL")) {
                                String maxTemperature = jedis.lrange(String.format("%s:TEMPERATURES:%s", Constantes.INICIALES, split[1]), 0, -1).stream().max(String::compareTo).get();
                                System.out.printf("Esta es la temperatura máxima registrada  %s\n", maxTemperature);
                            } else {
                                //obtiene la temperatura máxima de la estación
                                String maxTemperature = jedis.lrange(String.format("%s:TEMPERATURES:%s", Constantes.INICIALES, split[1]), 0, -1).stream().max(String::compareTo).get();
                                System.out.printf("Esta es la temperatura máxima registrada por la estación %s: %s\n", split[1], maxTemperature);
                            }
                        } catch (Exception e) {
                            System.out.printf("No existe la estación %s\n", split[1]);
                        }
                    }
                    case "ALERTS" -> {
                        //obtiene las alertas
                        Set<String> keys = jedis.keys(Constantes.ALERTS_KEY_REDIS);
                        if (keys != null) {
                            for (String key : keys) {
                                String alert = jedis.get(key);
                                System.out.println(alert);
                                jedis.del(key);
                            }
                        }
                    }
                    case "STOP" -> {
                        //detiene la estación
                        String id = split[1];
                        System.out.println("La estación " + id + " ha sido detenida");
                        jedis.set(id, "STOP");
                    }
                }
            }
        }

    }
}
