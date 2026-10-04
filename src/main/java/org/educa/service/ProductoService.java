package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;
import generated.Producto;
import generated.Productos;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.SummaryEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.text.ParseException;



public class ProductoService {
    private ProductoDAO productoDAO = new ProductoDAOImpl();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {

        File file = new File(fileXml);

        Productos productos = productoDAO.readFile(file);

        List<ProductoEntity> lista = new ArrayList<>();

        for (Producto producto : productos.getProducto()) {

            ProductoEntity productoEntity = new ProductoEntity();

            productoEntity.setProducto(producto);

            BigDecimal descuento = producto.getPrecio()
                    .multiply(producto.getDescuento())
                    .divide(BigDecimal.valueOf(100));

            BigDecimal precioFinal = producto.getPrecio().subtract(descuento);

            BigDecimal coste = producto.getCostes().getCostesEnvio()
                    .add(producto.getCostes().getCostesAlmacenaje());

            BigDecimal beneficio = precioFinal.subtract(coste);

            productoEntity.setPrecioFinal(precioFinal);
            productoEntity.setCost(coste);
            productoEntity.setProfit(beneficio);

            lista.add(productoEntity);
        }

        return lista;
    }


    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        File file = new File(fileXml);

        // Leemos los productos del XML
        List<ProductoEntity> productos = readFile(fileXml);

        // Calculamos el beneficio total
        BigDecimal totalProfit = BigDecimal.ZERO;

        for (ProductoEntity producto : productos) {
            totalProfit = totalProfit.add(producto.getProfit());
        }

        // Obtenemos el nombre del periodo
        String nombreFichero = file.getName().replace(".xml", "");
        String nombre = nombreFichero.replace("inventario_", "");

        // Creamos el objeto resumen
        SummaryEntity summary = new SummaryEntity(
                nombre,
                productos.size(),
                totalProfit,
                file.getAbsolutePath(),
                file.getName(),
                file.length()
        );

        // Creamos la carpeta de exportación si no existe
        File directorio = new File(path);

        if (!directorio.exists()) {
            directorio.mkdirs();
        }

        // Creamos el fichero TXT
        File ficheroResumen = new File(path + "result_" + nombre + ".txt");

        try (FileWriter fileWriter = new FileWriter(ficheroResumen);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {

            bufferedWriter.write(summary.toPrint());
        }
    }


    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}

