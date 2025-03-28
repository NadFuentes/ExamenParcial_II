/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package examenparcial_ii;

/**
 *
 * @author Nadiesda Fuentes
 */

public enum Trophy {
    PLATINO(5),
    ORO(3),
    PLATA(2),
    BRONCE(1);

    private final int points;

    Trophy(int points) {
        this.points = points;
    }

    public int getPoints() {
        return points;
    }

    public static Trophy getByPoints(int points) {
        for (Trophy t : values()) {
            if (t.getPoints() == points) {
                return t;
            }
        }
        throw new IllegalArgumentException("No existe un trofeo con " + points + " puntos");
    }
}