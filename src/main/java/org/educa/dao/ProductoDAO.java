package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBException;

import java.io.File;

public interface ProductoDAO {
    Productos readFile(File file) throws JAXBException;
}
