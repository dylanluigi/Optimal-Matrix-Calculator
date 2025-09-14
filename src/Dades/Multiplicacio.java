package Dades;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Implementa la multiplicació de matrius amb l'algorisme de Strassen i paral·lelisme mitjançant ForkJoinPool.
 */
public class Multiplicacio extends RecursiveTask<Matriu> {
    private static final int UMBRAL_STRASSEN = 64;
    private static final int MAX_PROFUNDITAT = 3;
    private static final ForkJoinPool pool = new ForkJoinPool();

    private final Matriu A;
    private final Matriu B;
    private final int profunditat;

    /**
     * Constructor públic per a ús extern
     * @param A Matriu A
     * @param B Matriu B
     */
    public Multiplicacio(Matriu A, Matriu B) {
        this(A, B, 0);
    }

    private Multiplicacio(Matriu A, Matriu B, int profunditat) {
        this.A = A;
        this.B = B;
        this.profunditat = profunditat;
    }

    /**
     * Executa l'algorisme de Strassen o la multiplicació clàssica segons el llindar i la profunditat.
     * @return Matriu resultant de la multiplicació.
     */
    @Override
    public Matriu compute() {
        int mida = A.getSize();

        // Cas base: multiplicació clàssica
        if (mida <= UMBRAL_STRASSEN || profunditat > MAX_PROFUNDITAT) {
            return multiplicacioClassicaOptimitzada(A, B);
        }

        // Ajustar mida imparella
        Matriu[] ajustades = ajustarMida(A, B);
        Matriu A_ajustada = ajustades[0];
        Matriu B_ajustada = ajustades[1];
        int novaMida = A_ajustada.getSize();

        // Dividir en submatrius
        Matriu[] subA = dividirMatriu(A_ajustada, novaMida / 2);
        Matriu[] subB = dividirMatriu(B_ajustada, novaMida / 2);

        // Crear subtasques amb control de profunditat
        Multiplicacio[] tasques = {
                new Multiplicacio(subA[0].add(subA[3]), subB[0].add(subB[3]), profunditat + 1),
                new Multiplicacio(subA[2].add(subA[3]), subB[0], profunditat + 1),
                new Multiplicacio(subA[0], subB[1].subtract(subB[3]), profunditat + 1),
                new Multiplicacio(subA[3], subB[2].subtract(subB[0]), profunditat + 1),
                new Multiplicacio(subA[0].add(subA[1]), subB[3], profunditat + 1),
                new Multiplicacio(subA[2].subtract(subA[0]), subB[0].add(subB[1]), profunditat + 1),
                new Multiplicacio(subA[1].subtract(subA[3]), subB[2].add(subB[3]), profunditat + 1)
        };

        // Fork de les dues primeres tasques
        tasques[0].fork();
        tasques[1].fork();

        // Computar les altres seqüencialment
        Matriu P3 = tasques[2].compute();
        Matriu P4 = tasques[3].compute();
        Matriu P5 = tasques[4].compute();
        Matriu P6 = tasques[5].compute();
        Matriu P7 = tasques[6].compute();

        // Esperar resultats de les tasques fork
        Matriu P1 = tasques[0].join();
        Matriu P2 = tasques[1].join();

        // Combinar resultats
        Matriu C11 = P1.add(P4).subtract(P5).add(P7);
        Matriu C12 = P3.add(P5);
        Matriu C21 = P2.add(P4);
        Matriu C22 = P1.add(P3).subtract(P2).add(P6);

        Matriu resultat = Matriu.combine(C11, C12, C21, C22);
        return retallarSiCal(resultat, A.getSize());
    }

    /**
     * Multiplicació clàssica optimitzada amb tècnica de tiling per a millorar l'accés a la cache.
     * @param a Matriu A.
     * @param b Matriu B.
     * @return Matriu resultant.
     */
    private Matriu multiplicacioClassicaOptimitzada(Matriu a, Matriu b) {
        int n = a.getSize();
        Matriu resultat = new Matriu(n);
        int[] aData = a.getData();
        int[] bData = b.getData();
        int[] cData = resultat.getData();

        for (int i = 0; i < n; i += UMBRAL_STRASSEN) {
            for (int j = 0; j < n; j += UMBRAL_STRASSEN) {
                for (int k = 0; k < n; k += UMBRAL_STRASSEN) {
                    processarTile(aData, bData, cData, n, i, j, k);
                }
            }
        }
        return resultat;
    }

    /**
     * Processa un bloc (tile) de la matriu per optimitzar l'ús de registres de la CPU.
     * @param a Dades de la matriu A.
     * @param b Dades de la matriu B.
     * @param c Dades de la matriu resultat.
     * @param n Mida de les matrius.
     * @param iBase Índex inicial de fila.
     * @param jBase Índex inicial de columna.
     * @param kBase Índex inicial per a la suma.
     */
    private void processarTile(int[] a, int[] b, int[] c, int n, int iBase, int jBase, int kBase) {
        int iLimit = Math.min(iBase + UMBRAL_STRASSEN, n);
        int jLimit = Math.min(jBase + UMBRAL_STRASSEN, n);
        int kLimit = Math.min(kBase + UMBRAL_STRASSEN, n);

        for (int i = iBase; i < iLimit; i++) {
            for (int k = kBase; k < kLimit; k++) {
                int aVal = a[i * n + k];
                for (int j = jBase; j < jLimit; j++) {
                    c[i * n + j] += aVal * b[k * n + j];
                }
            }
        }
    }

    /**
     * Ajusta les matrius a mida parell afegint zeros si cal.
     * @param A Matriu A.
     * @param B Matriu B.
     * @return Matrius ajustades.
     */
    private Matriu[] ajustarMida(Matriu A, Matriu B) {
        if (A.getSize() % 2 == 0) return new Matriu[]{A, B};

        int novaMida = A.getSize() + 1;
        return new Matriu[]{padMatrix(A, novaMida), padMatrix(B, novaMida)};
    }

    /**
     * Afegeix una fila/columna de zeros
     */
    private Matriu padMatrix(Matriu original, int novaMida) {
        Matriu padded = new Matriu(novaMida);
        for (int i = 0; i < original.getSize(); i++) {
            System.arraycopy(
                    original.getData(),
                    i * original.getSize(),
                    padded.getData(),
                    i * novaMida,
                    original.getSize()
            );
        }
        return padded;
    }

    /**
     * Retalla la matriu a la mida original si s'ha ajustat
     */
    private Matriu retallarSiCal(Matriu matriu, int midaOriginal) {
        if (matriu.getSize() == midaOriginal) return matriu;

        Matriu retallada = new Matriu(midaOriginal);
        for (int i = 0; i < midaOriginal; i++) {
            System.arraycopy(
                    matriu.getData(),
                    i * matriu.getSize(),
                    retallada.getData(),
                    i * midaOriginal,
                    midaOriginal
            );
        }
        return retallada;
    }

    /**
     * Divideix una matriu en 4 submatrius
     */
    private Matriu[] dividirMatriu(Matriu matriu, int novaMida) {
        return new Matriu[]{
                matriu.getSubMatrix(0, 0, novaMida),
                matriu.getSubMatrix(0, novaMida, novaMida),
                matriu.getSubMatrix(novaMida, 0, novaMida),
                matriu.getSubMatrix(novaMida, novaMida, novaMida)
        };
    }

    /**
     * Combina 4 submatrius en una de gran
     */
    private Matriu combinarResultats(Matriu C11, Matriu C12, Matriu C21, Matriu C22) {
        return Matriu.combine(C11, C12, C21, C22);
    }
}