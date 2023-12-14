package meteo.meteo_service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MeteoStationsPool {
    /**
     * Crea un pool de hilos
     * que vendrian siendo las estaciones
     * @param args
     */
    public static void main(String[] args) {
        int maxThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(maxThreads);
        for (int i = 0; i < maxThreads; i++){
            executor.execute(new MeteoStations());
        }
    }
}
