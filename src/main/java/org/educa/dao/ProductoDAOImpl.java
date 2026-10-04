package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

public class ProductoDAOImpl implements ProductoDAO {

    @Override
    public Productos readFile(File file) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Productos.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        return (Productos) unmarshaller.unmarshal(file);
    }

}
