package org.example.dragon.resource;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Разбор XML-тела, который не прощает ошибок: по умолчанию JAXB молча превращает
 * некорректное число или неизвестный элемент в null/пропуск. Здесь любое событие-ошибка
 * прерывает разбор и клиент получает 400. Внешние сущности и DTD отключены (защита от XXE).
 */
@Provider
@Consumes({MediaType.APPLICATION_XML, MediaType.TEXT_XML})
public class StrictXmlBodyReader implements MessageBodyReader<Object> {

    private static final Map<Class<?>, JAXBContext> CONTEXTS = new ConcurrentHashMap<>();

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException {
        XMLStreamReader reader = null;
        try {
            XMLInputFactory factory = XMLInputFactory.newFactory();
            factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            reader = factory.createXMLStreamReader(entityStream);

            Unmarshaller unmarshaller = CONTEXTS.computeIfAbsent(type, StrictXmlBodyReader::newContext)
                    .createUnmarshaller();
            unmarshaller.setEventHandler(event -> false);
            return unmarshaller.unmarshal(reader);
        } catch (JAXBException | XMLStreamException e) {
            throw new BadRequestException("Malformed XML: " + rootMessage(e));
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException ignored) {
                    // уже отдали результат или ошибку
                }
            }
        }
    }

    private static JAXBContext newContext(Class<?> type) {
        try {
            return JAXBContext.newInstance(type);
        } catch (JAXBException e) {
            throw new IllegalStateException("Cannot create JAXB context for " + type, e);
        }
    }

    private static String rootMessage(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : error.toString();
    }
}
