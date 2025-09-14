# Matrix Operations Benchmark (MVC + Concurrency + Strassen)

A small, teaching-oriented Java application to analyze and visualize the computational cost of matrix operations. It includes a Swing GUI, an MVC architecture, and concurrent implementations of Matrix Addition and Matrix Multiplication (Strassen + ForkJoin) with live charts of runtime and hidden constants.

> Built as a course project; the accompanying technical report (in Catalan) details design choices, algorithms, and results.

---

## Features

* Interactive GUI (Swing + JFreeChart) to:
  * Choose initial matrix size and step.
  * Enable/disable **Addition** and **Multiplication** runs.
  * Optionally use a **fixed RNG seed** for reproducible benchmarks.
  * See two live charts:
    * **Runtime vs. dimension** (Addition & Multiplication).
    * **Hidden constant vs. dimension** (log-scale):
      * Addition constant ≈ `time / n²`
      * Multiplication constant ≈ `time / n³`
* **Clean MVC design** with an **event/callback interface** (`Notificar`) between View and Controller.
* **Concurrency baked in**:

  * Addition: **ExecutorService + CountDownLatch**, cache-friendly tiling.
  * Multiplication: **Strassen’s algorithm** with **ForkJoinPool**, depth/threshold controls.
* **Didactic focus** on **asymptotic analysis** and practical performance trade-offs.&#x20;

---

## Architecture (MVC + events)

```
Vista (Swing GUI)
  └─ GUIOperacionsMatrius
        ↑        ↓  (events via Notificar)
Controladora
  └─ CalculMatricial (main)  ← orchestrates runs, threads, charts
        ↓
Dades (Model)
  ├─ Matriu            (matrix storage & helpers)
  ├─ Suma              (parallel addition)
  └─ Multiplicacio     (Strassen + ForkJoin)
```

* **View ↔ Controller**: decoupled via the `Notificar` interface (`onCalculationStarted`, `onCalculationCompleted`, `onCalculationError`, `onCalculationStopped`, …).
* **Controller ↔ Model**: controller invokes operations and streams back results to the GUI, computing hidden constants on the fly.

---

## Algorithms & complexity

* **Matrix Addition**: classic, parallelized → **Θ(n²)**; constant approximated as `time / n²`.
* **Matrix Multiplication (naïve)**: classic triple-loop → **Θ(n³)**.
* **Matrix Multiplication (Strassen)**: divide-and-conquer → **O(n^2.8074)**; implemented with **ForkJoinPool**, switching to classic below a threshold for practical speedups.
* The app emphasizes **theory vs. practice**: Strassen wins asymptotically, but thresholds, memory traffic, and thread overhead matter at moderate sizes.

---

## Credits

* **Dylan Canning Garcia** and collaborators (see the report’s author list). 

---

### Citation (if you use this in teaching/research)

> Canning Garcia, D., et al. *Matrix Operations Benchmark (MVC, concurrency, Strassen).* Project code and report. University coursework, 2025.

