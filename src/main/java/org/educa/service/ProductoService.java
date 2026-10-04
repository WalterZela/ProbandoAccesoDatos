package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;
import generated.Producto;
import generated.Productos;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import java.io.File;
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
        //TODO: Implementar

    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
