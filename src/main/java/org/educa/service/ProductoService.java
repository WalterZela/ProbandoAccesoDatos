package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.educa.entity.ProductoEntity;
import generated.Producto;
import generated.Productos;
import org.educa.dao.ProductoDAO;
import org.educa.dao.ProductoDAOImpl;
import org.educa.entity.SummaryEntity;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

        List<ProductoEntity> productos = readFile(fileXml);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        Row cabecera = sheet.createRow(0);

        cabecera.createCell(0).setCellValue("Codigo");
        cabecera.createCell(1).setCellValue("Numero de Serie");
        cabecera.createCell(2).setCellValue("Precio");
        cabecera.createCell(3).setCellValue("Descuento");
        cabecera.createCell(4).setCellValue("Precio Final");
        cabecera.createCell(5).setCellValue("Costes Envio");
        cabecera.createCell(6).setCellValue("Costes Almacenaje");
        cabecera.createCell(7).setCellValue("Beneficio");

        CellStyle estiloCabecera = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);

        estiloCabecera.setFont(font);
        estiloCabecera.setAlignment(HorizontalAlignment.CENTER);
        estiloCabecera.setBorderTop(BorderStyle.THIN);
        estiloCabecera.setBorderBottom(BorderStyle.THIN);
        estiloCabecera.setBorderLeft(BorderStyle.THIN);
        estiloCabecera.setBorderRight(BorderStyle.THIN);

        for (Cell cell : cabecera) {
            cell.setCellStyle(estiloCabecera);
        }

        CellStyle estiloFila1 = workbook.createCellStyle();
        estiloFila1.setAlignment(HorizontalAlignment.CENTER);
        estiloFila1.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estiloFila1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloFila1.setBorderTop(BorderStyle.THIN);
        estiloFila1.setBorderBottom(BorderStyle.THIN);
        estiloFila1.setBorderLeft(BorderStyle.THIN);
        estiloFila1.setBorderRight(BorderStyle.THIN);

        CellStyle estiloFila2 = workbook.createCellStyle();
        estiloFila2.setAlignment(HorizontalAlignment.CENTER);
        estiloFila2.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        estiloFila2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estiloFila2.setBorderTop(BorderStyle.THIN);
        estiloFila2.setBorderBottom(BorderStyle.THIN);
        estiloFila2.setBorderLeft(BorderStyle.THIN);
        estiloFila2.setBorderRight(BorderStyle.THIN);

        int fila = 1;


        for (ProductoEntity producto : productos ){
            Row row = sheet.createRow(fila);

            row.createCell(0).setCellValue(producto.getProducto().getCodigo());
            row.createCell(1).setCellValue(producto.getProducto().getNumeroSerie());
            row.createCell(2).setCellValue(producto.getProducto().getPrecio().doubleValue());
            row.createCell(3).setCellValue(producto.getProducto().getDescuento().doubleValue());
            row.createCell(4).setCellValue(producto.getPrecioFinal().doubleValue());
            row.createCell(5).setCellValue(producto.getProducto().getCostes().getCostesEnvio().doubleValue());
            row.createCell(6).setCellValue(producto.getProducto().getCostes().getCostesAlmacenaje().doubleValue());
            row.createCell(7).setCellValue(producto.getProfit().doubleValue());

            CellStyle estilo;

            if (fila%2==0){
                estilo = estiloFila1;
            }else {
                estilo = estiloFila2;
            }
            for (Cell cell : row){
                cell.setCellStyle(estilo);
            }
    
            fila++;

        }
        for (int i = 0; i < 8; i++) {
            sheet.autoSizeColumn(i);
        }

        File ficheroExcel = new File(path + "export_junio2026.xlsx");
        FileOutputStream archivo = new FileOutputStream(ficheroExcel);

        workbook.write(archivo);

        archivo.close();
        workbook.close();

    }
}
