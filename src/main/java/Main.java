import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        String fileNameJsonFromCSV = "data.json";
        String fileNameJsonFromXML = "data2.json";
        List<Employee> listFromCSV = parseCSV(columnMapping, fileName);
        String jsonFromCSV = listToJson(listFromCSV);
        writeString(jsonFromCSV, fileNameJsonFromCSV);

        List<Employee> listFromXML = parseXML("data.xml");
        String jsonFromXML = listToJson(listFromXML);
        writeString(jsonFromXML, fileNameJsonFromXML);

        String json = readString(fileNameJsonFromCSV);
        List<Employee> list = jsonToList(json);
        for (Employee employee : list) {
            System.out.println(employee);
        }
    }

    private static List<Employee> jsonToList(String json) {
        List<Employee> employees = new ArrayList<>();
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray jsonArray = (JSONArray) jsonParser.parse(json);
            for (int i = 0; i < jsonArray.size(); i++) {
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                Employee employee = gson.fromJson(jsonArray.get(i).toString(), Employee.class);
                employees.add(employee);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return employees;
    }

    private static String readString(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static List<Employee> parseXML(String fileName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new File(fileName));
            Node root = document.getDocumentElement();
            ArrayList<Employee> employees = new ArrayList<Employee>();
            read(root, employees);
            return employees;
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
            return new ArrayList<Employee>();
        }
    }

    private static void read(Node NodeIncoming, ArrayList<Employee> employees) {
        NodeList nodeList = NodeIncoming.getChildNodes();

        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) tempNode;
                employees.add(new Employee(
                        Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent()),
                        element.getElementsByTagName("firstName").item(0).getTextContent(),
                        element.getElementsByTagName("lastName").item(0).getTextContent(),
                        element.getElementsByTagName("country").item(0).getTextContent(),
                        Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent())));
            }
        }
    }

    private static void writeString(String json, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName, false)) {
            fileWriter.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.toJson(list, listType);
    }

    private static List<Employee> parseCSV(String[] columnMapping, String fileName) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fileName))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader).withMappingStrategy(strategy).build();
            List<Employee> staff = csv.parse();
            return staff;
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<Employee>();
        }
    }
}
