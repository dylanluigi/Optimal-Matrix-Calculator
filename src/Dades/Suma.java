package Dades;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

/**
 * Implementa la suma paral·lelitzada de matrius amb optimització de cache.
 */

public class Suma {
    private static final int CACHE_LINE_SIZE = 64; // 64 bytes per línia de cache
    private static final int INTS_PER_CACHE_LINE = CACHE_LINE_SIZE / Integer.BYTES; // 16 enters per línia

    /**
     * Suma dues matrius utilitzant tots els nuclis disponibles.
     * @param a Matriu A.
     * @param b Matriu B.
     * @return Matriu resultat.
     * @throws InterruptedException Si es interromp l'espera.
     */
    public static Matriu add(Matriu a, Matriu b) throws InterruptedException {
        return add(a, b, Runtime.getRuntime().availableProcessors());
    }

    /**
     * Suma dues matrius amb un nombre específic de fils i blocs alineats a la cache.
     * @param a Matriu A.
     * @param b Matriu B.
     * @param threads Nombre de fils.
     * @return Matriu resultat.
     * @throws InterruptedException Si es interromp l'espera.
     */
    public static Matriu add(Matriu a, Matriu b, int threads) throws InterruptedException {
        if (a.getSize() != b.getSize()) {
            throw new IllegalArgumentException("Dimensions no coincideixen");
        }

        Matriu c = new Matriu(a.getSize());
        int[] aData = a.getData();
        int[] bData = b.getData();
        int[] cData = c.getData();
        int totalElements = aData.length;

        ExecutorService executor = Executors.newFixedThreadPool(threads);

        // Calcula la mida òptima dels blocs segons la cache
        int blocSize = Math.max(totalElements / threads, INTS_PER_CACHE_LINE);
        blocSize = (blocSize + INTS_PER_CACHE_LINE - 1) & ~(INTS_PER_CACHE_LINE - 1); // Alineat a la cache

        // Calcula el nombre real de blocs necessaris
        int numBlocks = (totalElements + blocSize - 1) / blocSize;
        CountDownLatch latch = new CountDownLatch(numBlocks);

        for (int t = 0; t < numBlocks; t++) {
            final int start = t * blocSize;
            final int end = Math.min(start + blocSize, totalElements);

            executor.execute(() -> {
                int i = start;
                int limit = end - 7;

                // Bloc principal amb unrolling 8x
                for (; i < limit; i += 8) {
                    cData[i]   = aData[i]   + bData[i];
                    cData[i+1] = aData[i+1] + bData[i+1];
                    cData[i+2] = aData[i+2] + bData[i+2];
                    cData[i+3] = aData[i+3] + bData[i+3];
                    cData[i+4] = aData[i+4] + bData[i+4];
                    cData[i+5] = aData[i+5] + bData[i+5];
                    cData[i+6] = aData[i+6] + bData[i+6];
                    cData[i+7] = aData[i+7] + bData[i+7];
                }

                // Elements residuals
                for (; i < end; i++) {
                    cData[i] = aData[i] + bData[i];
                }

                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        return c;
    }

    /**
     * Calcula la constant de rendiment per a sumes.
     * @param t Temps d'execució.
     * @param n Mida de la matriu.
     * @return Constant calculada (temps / n²).
     */
    public static double constante(double t, int n){
        return (t/(double)(n*n));
    }
}