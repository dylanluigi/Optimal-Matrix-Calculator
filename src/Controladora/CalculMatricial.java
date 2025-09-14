package Controladora;

import Dades.*;
import Vista.GUIOperacionsMatrius;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * Classe controladora que gestiona els càlculs matricials i la comunicació amb la vista.
 * Implementa el patró MVC i gestiona la lògica de negoci dels benchmarks.
 */
public class CalculMatricial implements Notificar {
    private GUIOperacionsMatrius gui;
    private boolean isRunning = false;
    private Thread calculationThread;
    private ExecutorService executor;
    private ForkJoinPool forkJoinPool;

    // Estructures de dades per emmagatzemar resultats
    private List<BenchmarkResult> additionResults = new ArrayList<>();
    private List<BenchmarkResult> multiplicationResults = new ArrayList<>();

    /**
     * Classe interna per emmagatzemar resultats individuals de benchmarks
     */
    private static class BenchmarkResult {
        final int dimension;
        final double time;
        final double constant;

        /**
         * Constructor que inicialitza un resultat de benchmark
         * @param dimension Mida de la matriu
         * @param time Temps d'execució en mil·lisegons
         * @param constant Constant de rendiment calculada
         */
        BenchmarkResult(int dimension, double time, double constant) {
            this.dimension = dimension;
            this.time = time;
            this.constant = constant;
        }
    }

    /**
     * Constructor principal que inicialitza la interfície gràfica
     */
    public CalculMatricial() {
        this.gui = new GUIOperacionsMatrius(this);
    }

    /**
     * Notifica l'inici d'un càlcul. Configura els paràmetres inicials, reinicia estats i llança un fil de càlcul.
     * @param initialDimension Mida inicial de les matrius.
     * @param stepping Increment de mida per iteració.
     * @param sumEnabled Habilita l'operació de suma.
     * @param multiplicationEnabled Habilita l'operació de multiplicació.
     * @param useFixedSeed Ús de llavor fixa per a valors reproduïbles.
     */
    @Override
    public void onCalculationStarted(int initialDimension, int stepping, boolean sumEnabled,
                                     boolean multiplicationEnabled, boolean useFixedSeed) {
        if (isRunning) return;

        // Reiniciar estats i dades
        additionResults.clear();
        multiplicationResults.clear();
        isRunning = true;
        gui.setCalculationRunning(true);

        // Configurar i iniciar fil de càlcul
        calculationThread = new Thread(() -> {
            try {
                runCalculations(initialDimension, stepping, sumEnabled, multiplicationEnabled, useFixedSeed);
            } catch (Exception e) {
                onCalculationError("Error during calculation: " + e.getMessage());
            }
        });

        calculationThread.start();
    }

    /**
     * Atura tots els processos actius (fils, executors) i notifica la vista per deshabilitar controls.
     */
    @Override
    public void onCalculationStopped() {
        // Aturar recursos de forma segura
        isRunning = false;
        if (calculationThread != null && calculationThread.isAlive()) {
            calculationThread.interrupt();
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        gui.setCalculationRunning(false);
    }

    /**
     * Actualitza la vista amb els resultats d'una operació completada i emmagatzema les dades.
     * @param operationType Tipus d'operació ("Addition" o "Multiplication").
     * @param dimension Mida de la matriu processada.
     * @param executionTime Temps d'execució en mil·lisegons.
     * @param constant Constant de rendiment calculada.
     */
    @Override
    public void onCalculationCompleted(String operationType, int dimension, double executionTime, double constant) {
        // Actualitzar vista i emmagatzemar resultats
        gui.addDataPoint(operationType, dimension, executionTime, constant);
        if ("Addition".equals(operationType)) {
            additionResults.add(new BenchmarkResult(dimension, executionTime, constant));
        } else if ("Multiplication".equals(operationType)) {
            multiplicationResults.add(new BenchmarkResult(dimension, executionTime, constant));
        }
    }

    /**
     * Gestiona errors durant el càlcul, notifica la vista i reinicia estats.
     * @param errorMessage Missatge d'error a mostrar.
     */
    @Override
    public void onCalculationError(String errorMessage) {
        // Gestionar errors i notificar a la vista
        isRunning = false;
        gui.showError(errorMessage);
        gui.setCalculationRunning(false);
    }

    /**
     * Executa iterativament els càlculs de les operacions seleccionades, augmentant la mida de les matrius.
     * @param initialDimension Mida inicial de les matrius.
     * @param stepping Increment de mida per iteració.
     * @param sumEnabled Indica si s'executen sumes.
     * @param multiplicationEnabled Indica si s'executen multiplicacions.
     * @param useFixedSeed Ús de llavor fixa per a valors reproduïbles.
     */
    private void runCalculations(int initialDimension, int stepping, boolean sumEnabled,
                                 boolean multiplicationEnabled, boolean useFixedSeed) {
        java.util.Random random = useFixedSeed ? new java.util.Random(42) : new java.util.Random();
        int n = initialDimension;

        // Use a single ForkJoinPool for all multiplication tasks
        forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

        while (isRunning) {
            Matriu matriuA = new Matriu(n);
            Matriu matriuB = new Matriu(n);
            matriuA.initRandom();
            matriuB.initRandom();

            // Execució de sumes
            if (sumEnabled) {
                int finalN = n;
                new Suma();
                processOperation(matriuA, matriuB, n, "Addition",
                        (a, b) -> Suma.add(a, b),
                        time -> Suma.constante(time, finalN));
            }

            // Execució de multiplicacions
            if (multiplicationEnabled) {
                int finalN1 = n;
                processOperation(matriuA, matriuB, n, "Multiplication",
                        (a, b) -> new Multiplicacio(a, b).compute(), // Crida directa sense ForkJoinPool extern
                        time -> constanteMult(time, finalN1));
            }

            n += stepping;
            pauseCalculation();
        }

        // Shutdown the ForkJoinPool when done
        forkJoinPool.shutdown();
        shutdownResources();
    }

    /**
     * Processa una operació matricial genèrica.
     * @param a Matriu A.
     * @param b Matriu B.
     * @param dimension Mida actual de les matrius.
     * @param operationName Nom de l'operació per a registres.
     * @param operation Implementació de l'operació matricial.
     * @param constantCalculator Càlcul de la constant de rendiment.
     */
    private void processOperation(Matriu a, Matriu b, int dimension, String operationName,
                                  MatrixOperation operation, ConstantCalculator constantCalculator) {
        if (!isRunning) return;

        try {
            long start = System.nanoTime();
            operation.execute(a, b);
            double time = (System.nanoTime() - start) / 1_000_000.0;
            double constant = constantCalculator.calculate(time);
            onCalculationCompleted(operationName, dimension, time, constant);
        } catch (Exception e) {
            onCalculationError("Error en " + operationName + ": " + e.getMessage());
        }
    }

    /**
     * Interfície funcional per a operacions matricials
     */
    @FunctionalInterface
    private interface MatrixOperation {
        void execute(Matriu a, Matriu b) throws Exception;
    }

    /**
     * Interfície funcional per al càlcul de constants
     */
    @FunctionalInterface
    private interface ConstantCalculator {
        double calculate(double time);
    }

    /**
     * Pausa entre iteracions per permetre la resposta de la UI
     */
    private void pauseCalculation() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Alliberament controlat de recursos
     */
    private void shutdownResources() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdown();
        }
        if (isRunning) {
            isRunning = false;
            gui.setCalculationRunning(false);
        }
    }

    public static double constanteMult(double t, int n){
        return (t/(double)(n*n*n));
    }

    public static void main(String[] args) {
        new CalculMatricial();
    }
}