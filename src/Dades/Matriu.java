package Dades;

import java.util.Random;
import java.util.Arrays;

/**
 * Representa una matriu amb operacions bàsiques.
 */
public class Matriu {
    private final int size;
    private final int[] data;

    public Matriu(int size) {
        this.size = size;
        this.data = new int[size * size];
    }

    /**
     * Inicialitza la matriu amb valors aleatoris.
     */
    public void initRandom() {
        Random random = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = random.nextInt();
        }
    }

    public int get(int i, int j) {
        return data[i * size + j];
    }

    public void set(int i, int j, int value) {
        data[i * size + j] = value;
    }

    /**
     * Retorna la mida de la matriu.
     * @return Mida de la matriu.
     */
    public int getSize() {
        return size;
    }

    /**
     * Retorna la matriu com a vector.
     * @return Vector de la matriu.
     */
    public int[] getData() {
        return data;
    }

    /**
     * Crea una submatriu des de les coordenades especificades.
     * @param row Fila inicial.
     * @param col Columna inicial.
     * @param newSize Mida de la submatriu.
     * @return Submatriu creada.
     */
    public Matriu getSubMatrix(int row, int col, int newSize) {
        Matriu subMatriu = new Matriu(newSize);
        for (int i = 0; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                subMatriu.set(i, j, this.get(row + i, col + j));
            }
        }
        return subMatriu;
    }

    /**
     * Suma aquesta matriu amb una altra.
     * @param other Matriu a sumar.
     * @return Nova matriu resultat.
     */
    public Matriu add(Matriu other) {
        Matriu result = new Matriu(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = this.data[i] + other.data[i];
        }
        return result;
    }

    /**
     * Resta aquesta matriu amb una altra.
     * @param other Matriu a restar.
     * @return Nova matriu resultat.
     */
    public Matriu subtract(Matriu other) {
        Matriu result = new Matriu(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = this.data[i] - other.data[i];
        }
        return result;
    }

    /**
     * Combina quatre submatrius en una matriu gran.
     * @param c11 Submatriu superior-esquerra.
     * @param c12 Submatriu superior-dreta.
     * @param c21 Submatriu inferior-esquerra.
     * @param c22 Submatriu inferior-dreta.
     * @return Matriu combinada.
     */
    public static Matriu combine(Matriu c11, Matriu c12, Matriu c21, Matriu c22) {
        int newSize = c11.getSize() * 2;
        Matriu result = new Matriu(newSize);

        for (int i = 0; i < c11.getSize(); i++) {
            for (int j = 0; j < c11.getSize(); j++) {
                result.set(i, j, c11.get(i, j));
                result.set(i, j + c11.getSize(), c12.get(i, j));
                result.set(i + c11.getSize(), j, c21.get(i, j));
                result.set(i + c11.getSize(), j + c11.getSize(), c22.get(i, j));
            }
        }
        return result;
    }
}
