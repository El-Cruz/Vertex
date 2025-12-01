import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class PilaCarrito {
    private Stack<Producto> pila;

    public PilaCarrito(){
        this.pila = new Stack<>();
    }

    public void push(Producto p) {
        pila.push(p);
    }

    public void pop() {
        if (!pila.isEmpty()) {
            pila.pop();
        }
    }

    // Pop que devuelve el producto (para regresar stock)
    public Producto popYObtener() {
        if (pila.isEmpty()) return null;
        return pila.pop();
    }

    public boolean estaVacia() {
        return pila.isEmpty();
    }

    public double calcularTotal() {
        double total = 0;
        for (Producto p : pila) {
            total += p.getPrecio();
        }
        return total;
    }

    // Actualiza el modelo de la JList que muestra la pila/carrito
    public void actualizarModelo(DefaultListModel<String> modelo) {
        modelo.clear();
        // Mostrar de arriba hacia abajo (LIFO)
        for (int i = pila.size() - 1; i >= 0; i--) {
            Producto p = pila.get(i);
            modelo.addElement(p.toString());
        }
    }

    public List<Producto> getElementos() {
        return new ArrayList<>(pila);
    }

    public void vaciar() {
        pila.clear();
    }
}
