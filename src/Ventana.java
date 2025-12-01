import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.LinkedHashMap;
import java.util.Map;

public class Ventana extends JFrame {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JComboBox<String> comboBox1;
    private JTextField textField1;
    private JTable table1;
    private JButton BUSCARButton;
    private JButton NUEVOPRODUCTOButton;
    private JButton ACTUALIZARSTOCKButton;
    private JButton ELIMINARButton;
    private JList<String> list1;
    private JSpinner spinner1;
    private JComboBox<String> comboBox2;
    private JButton AGREGARALCARRITOButton;
    private JTable table2;
    private JButton CANCELARButton;
    private JButton PAGARButton;
    private JLabel lblSubTotal;
    private JLabel lblIva;
    private JLabel lblTotalPagar;

    private ListaProductos inventario;
    private PilaCarrito carrito;
    private DefaultTableModel modeloInventario;
    private DefaultTableModel modeloTicket;
    private DefaultListModel<String> modeloProductosDisponibles;

    public Ventana() {
        setTitle("Gestión Hardware - Progreso 2");
        setContentPane(panel1);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        inventario = new ListaProductos();
        carrito = new PilaCarrito();

        configurarModelos();
        cargarDatosIniciales();
        configurarEventos();
    }

    private void configurarModelos() {
        modeloInventario = new DefaultTableModel(
                new Object[]{"Nombre", "Categoría", "Stock", "Precio ($)"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table1.setModel(modeloInventario);

        comboBox1.removeAllItems();
        comboBox1.addItem("Todos");
        comboBox1.addItem("GPU");
        comboBox1.addItem("CPU");
        comboBox1.addItem("RAM");
        comboBox1.addItem("Monitor");
        comboBox1.addItem("Periférico");

        modeloProductosDisponibles = new DefaultListModel<>();
        list1.setModel(modeloProductosDisponibles);
        list1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        modeloTicket = new DefaultTableModel(
                new Object[]{"Producto", "Cantidad", "Precio U.", "Subtotal"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table2.setModel(modeloTicket);

        spinner1.setModel(new SpinnerNumberModel(1, 1, 100, 1));

        comboBox2.removeAllItems();
        comboBox2.addItem("Consumidor final");
        comboBox2.addItem("Empresa X");
        comboBox2.addItem("Empresa Y");

        actualizarTotales();
    }

    private void cargarDatosIniciales() {
        inventario.agregar(new Producto("RTX 4090", "GPU", 5, 1500.00));
        inventario.agregar(new Producto("Ryzen 9 7950X", "CPU", 8, 650.00));
        inventario.agregar(new Producto("16 GB DDR5", "RAM", 20, 90.00));
        inventario.agregar(new Producto("Monitor 24\"", "Monitor", 10, 220.00));
        inventario.agregar(new Producto("Teclado Mecánico", "Periférico", 15, 55.00));

        refrescarTablaInventario();
    }

    private void configurarEventos() {
        BUSCARButton.addActionListener(e -> refrescarTablaInventario());
        comboBox1.addActionListener(e -> refrescarTablaInventario());

        NUEVOPRODUCTOButton.addActionListener(e -> crearNuevoProducto());
        ACTUALIZARSTOCKButton.addActionListener(e -> actualizarStockSeleccionado());
        ELIMINARButton.addActionListener(e -> eliminarProductoSeleccionado());

        AGREGARALCARRITOButton.addActionListener(e -> agregarProductoAlCarrito());
        CANCELARButton.addActionListener(e -> deshacerUltimoDelCarrito());
        PAGARButton.addActionListener(e -> pagar());
    }

    private void refrescarTablaInventario() {
        modeloInventario.setRowCount(0);
        modeloProductosDisponibles.clear();

        String filtroCategoria = (String) comboBox1.getSelectedItem();
        if (filtroCategoria == null) filtroCategoria = "Todos";

        String textoBusqueda = textField1.getText().trim().toLowerCase();

        for (Producto p : inventario.getLista()) {
            boolean coincideCategoria = filtroCategoria.equals("Todos")
                    || p.getCategoria().equalsIgnoreCase(filtroCategoria);

            boolean coincideTexto = textoBusqueda.isEmpty()
                    || p.getNombre().toLowerCase().contains(textoBusqueda);

            if (coincideCategoria && coincideTexto) {
                modeloInventario.addRow(new Object[]{
                        p.getNombre(),
                        p.getCategoria(),
                        p.getStock(),
                        p.getPrecio()
                });

                if (p.getStock() > 0) {
                    modeloProductosDisponibles.addElement(
                            p.getNombre() + " | Stock: " + p.getStock() + " | $" + p.getPrecio()
                    );
                }
            }
        }
    }

    private void crearNuevoProducto() {
        String nombre = JOptionPane.showInputDialog(this, "Nombre del producto:");
        if (nombre == null || nombre.isBlank()) return;

        String[] opciones = {"GPU", "CPU", "RAM", "Monitor", "Periférico"};
        String categoria = (String) JOptionPane.showInputDialog(
                this, "Categoría:", "Categoría",
                JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
        if (categoria == null) return;

        int stock;
        double precio;
        try {
            String stockStr = JOptionPane.showInputDialog(this, "Stock inicial:", "1");
            if (stockStr == null) return;
            stock = Integer.parseInt(stockStr);

            String precioStr = JOptionPane.showInputDialog(this, "Precio unitario:", "0");
            if (precioStr == null) return;
            precio = Double.parseDouble(precioStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valores numéricos inválidos.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        inventario.agregar(new Producto(nombre, categoria, stock, precio));
        refrescarTablaInventario();
    }

    private void actualizarStockSeleccionado() {
        int fila = table1.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto del inventario.");
            return;
        }

        String nombre = (String) modeloInventario.getValueAt(fila, 0);
        Producto p = inventario.buscarPorNombre(nombre);
        if (p == null) return;

        try {
            String nuevoStockStr = JOptionPane.showInputDialog(
                    this, "Nuevo stock:", p.getStock());
            if (nuevoStockStr == null) return;
            int nuevoStock = Integer.parseInt(nuevoStockStr);
            p.setStock(nuevoStock);
            refrescarTablaInventario();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor de stock inválido.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarProductoSeleccionado() {
        int fila = table1.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto del inventario.");
            return;
        }

        String nombre = (String) modeloInventario.getValueAt(fila, 0);
        int opcion = JOptionPane.showConfirmDialog(
                this, "¿Eliminar el producto \"" + nombre + "\"?",
                "Confirmar", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            inventario.eliminarPorNombre(nombre);
            refrescarTablaInventario();
        }
    }

    private void agregarProductoAlCarrito() {
        String seleccion = list1.getSelectedValue();
        if (seleccion == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto de la lista disponible.");
            return;
        }

        String nombreProducto = seleccion.split(" \\| ")[0];
        Producto pInventario = inventario.buscarPorNombre(nombreProducto);
        if (pInventario == null) return;

        int cantidad = (Integer) spinner1.getValue();
        if (cantidad <= 0) return;

        if (cantidad > pInventario.getStock()) {
            JOptionPane.showMessageDialog(this, "No hay stock suficiente.");
            return;
        }

        pInventario.setStock(pInventario.getStock() - cantidad);
        refrescarTablaInventario();

        for (int i = 0; i < cantidad; i++) {
            carrito.push(new Producto(
                    pInventario.getNombre(),
                    pInventario.getCategoria(),
                    1,
                    pInventario.getPrecio()));
        }

        actualizarTablaTicketYTotales();
    }

    private void deshacerUltimoDelCarrito() {
        if (carrito.estaVacia()) {
            JOptionPane.showMessageDialog(this, "El carrito está vacío.");
            return;
        }

        Producto ultimo = carrito.popYObtener();
        if (ultimo != null) {
            Producto pInventario = inventario.buscarPorNombre(ultimo.getNombre());
            if (pInventario != null) {
                pInventario.setStock(pInventario.getStock() + 1);
                refrescarTablaInventario();
            }
        }
        actualizarTablaTicketYTotales();
    }

    private void actualizarTablaTicketYTotales() {
        modeloTicket.setRowCount(0);

        Map<String, Integer> cantidades = new LinkedHashMap<>();
        Map<String, Double> precios = new LinkedHashMap<>();

        for (Producto p : carrito.getElementos()) {
            String nombre = p.getNombre();
            cantidades.put(nombre, cantidades.getOrDefault(nombre, 0) + 1);
            precios.put(nombre, p.getPrecio());
        }

        double total = 0.0;
        for (String nombre : cantidades.keySet()) {
            int cant = cantidades.get(nombre);
            double precioU = precios.get(nombre);
            double subtotal = cant * precioU;
            total += subtotal;
            modeloTicket.addRow(new Object[]{nombre, cant, precioU, subtotal});
        }

        double iva = total * 0.15;
        double totalPagar = total + iva;

        lblSubTotal.setText(String.format("$ %.2f", total));
        lblIva.setText(String.format("$ %.2f", iva));
        lblTotalPagar.setText(String.format("$ %.2f", totalPagar));
    }

    private void actualizarTotales() {
        lblSubTotal.setText("$ 0.00");
        lblIva.setText("$ 0.00");
        lblTotalPagar.setText("$ 0.00");
    }

    private void pagar() {
        if (carrito.estaVacia()) {
            JOptionPane.showMessageDialog(this, "No hay productos en el carrito.");
            return;
        }

        String cliente = (String) comboBox2.getSelectedItem();
        if (cliente == null || cliente.isBlank()) cliente = "Sin nombre";

        double total = carrito.calcularTotal();
        double iva = total * 0.15;
        double totalPagar = total + iva;

        JOptionPane.showMessageDialog(this,
                "Cliente: " + cliente +
                        "\nSubtotal: $" + String.format("%.2f", total) +
                        "\nIVA (15%): $" + String.format("%.2f", iva) +
                        "\nTotal a pagar: $" + String.format("%.2f", totalPagar),
                "Pago realizado", JOptionPane.INFORMATION_MESSAGE);

        carrito.vaciar();
        actualizarTablaTicketYTotales();
        refrescarTablaInventario();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ventana().setVisible(true));
    }
}