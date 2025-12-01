import java.util.LinkedList;
import java.util.List;

public class ListaProductos {
    private LinkedList<Producto> lista;

    public ListaProductos(){
        this.lista = new LinkedList<>();
    }

    public void agregar(Producto p){
        lista.add(p);
    }

    public Producto[] obtenerArreglo() {
        return lista.toArray(new Producto[0]);
    }

    public List<Producto> getLista() {
        return lista;
    }

    public Producto buscarPorNombre(String nombre) {
        for (Producto p : lista) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                return p;
            }
        }
        return null;
    }

    public void eliminarPorNombre(String nombre) {
        lista.removeIf(p -> p.getNombre().equalsIgnoreCase(nombre));
    }
}
