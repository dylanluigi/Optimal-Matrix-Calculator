package Controladora;

/**
 * Defineix els mètodes de comunicació entre la vista, el controlador i el model.
 */
public interface Notificar {

    // Vista<->Controladora
    /**
     * Notifica l'inici d'un càlcul.
     * @param initialDimension Mida inicial de les matrius.
     * @param stepping Increment de mida per iteració.
     * @param sumEnabled Habilita l'operació de suma.
     * @param multiplicationEnabled Habilita l'operació de multiplicació.
     * @param useFixedSeed Ús de llavor fixa per a valors reproduïbles.
     */
    void onCalculationStarted(int initialDimension, int stepping, boolean sumEnabled,
                              boolean multiplicationEnabled, boolean useFixedSeed);

    /**
     * Notifica l'aturada forçosa del càlcul.
     */
    void onCalculationStopped();


    // Model <-> Controladora
    /**
     * Notifica la finalització exitosa d'una operació.
     * @param operationType Tipus d'operació ("Addition" o "Multiplication").
     * @param dimension Mida de la matriu processada.
     * @param executionTime Temps d'execució en mil·lisegons.
     * @param constant Constant de rendiment calculada.
     */
    void onCalculationCompleted(String operationType, int dimension, double executionTime, double constant);

    /**
     * Notifica errors durant el càlcul.
     * @param errorMessage Missatge d'error a mostrar.
     */
    void onCalculationError(String errorMessage);
}