//package model;
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
// 
// 
//import org.apache.tika.exception.TikaException;
//import org.apache.tika.metadata.Metadata;
//import org.apache.tika.parser.AutoDetectParser;
//import org.apache.tika.parser.ParseContext;
//import org.apache.tika.parser.Parser;
//import org.apache.tika.sax.BodyContentHandler;
//import org.xml.sax.ContentHandler;
//import org.xml.sax.SAXException;
// 
///**
// * Purpose of this class is taking file from client, parsing it with Tika Apache,
// * then giving the result to elastic search database
// * @author s.majkrzak
// *
// */
//public class FileParser {
// 
//        /**
//         * With using Tika Apache, this method takes all data from file,
//         * handle it in Metadata class object and BodyContentHandler class
//         * object, gives it to dataToArray method, and sends final result
//         * to Elastic Search database
//         * @param fileToParse file given by client, which have to be parsed
//         */
//    public void parseFile (File fileToParse){
//        InputStream is = null;
//       
//        try {
//                String fileName = fileToParse.getName();
//            is = new BufferedInputStream(new FileInputStream(fileToParse));
//            Parser parser = new AutoDetectParser();
//            ContentHandler handler = new BodyContentHandler();
//            Metadata metadata = new Metadata();
// 
//            parser.parse(is, handler, metadata, new ParseContext());
//            String [] result = dataToArray(metadata, handler, fileName);
//           
//            ElasticSearchManager esm = new ElasticSearchManager ();
//            esm.putJsonDocument(result);
// 
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (TikaException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch(IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//       
//    }
//    /**
//     * Extract the most important data for Elastic search and creates array with them
//     * @param metadata metadata of given file
//     * @param handler object with content of file
//     * @param fileName name of file used to creating temporary path of file
//     * @return Array with all interesting data for us
//     */
//    public String [] dataToArray (Metadata metadata, ContentHandler handler, String fileName) {
//        String [] data = new String [4];
//        data [0] = metadata.get("title");
//        data [1] = metadata.get("Author");
//        data [2] = handler.toString(); //content of file
//        data [3] = "C:/" + fileName;
//        return data;
//    }
//}