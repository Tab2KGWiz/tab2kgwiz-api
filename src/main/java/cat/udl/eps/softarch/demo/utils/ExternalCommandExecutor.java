package cat.udl.eps.softarch.demo.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class ExternalCommandExecutor {
    public static byte[] executeYARRRMLParser() {

        ProcessBuilder builder1 = new ProcessBuilder("docker", "run", "--rm", "-v",
                "C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\\static:/data",
                "rmlio/yarrrml-parser:latest", "-i", "/data/mappings.yarrrml.yml");

        // Redirect standard output (STDOUT) to a file
        builder1.redirectOutput(new File(
                "C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\\static\\rules.rml.ttl"));

        try {
            Process process = builder1.start();
            process.waitFor(); // Wait for the container to finish
            int exitCode = process.exitValue();

            if (exitCode == 0) {
                ProcessBuilder builder2 = new ProcessBuilder("docker", "run", "--rm", "-v",
                        "C:\\\\Users\\\\Zihan\\\\Desktop\\\\TFG\\\\tab2kgwiz-api\\\\src\\\\main\\\\static:/data", "rmlio/rmlmapper-java"
                        , "-m", "rules.rml.ttl");

                // Redirect standard output (STDOUT) to a file
//                builder2.redirectOutput(new File(
                        //"C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\    \static\\output.txt"));

                try {
                    Process process2 = builder2.start();
                    //process2.waitFor(); // Wait for the container to finish

                    int exitCode2 = process.exitValue();

                    if (exitCode2 == 0) {
                        System.out.println("RML mapper run successfully!");
                        InputStream stream = process2.getInputStream();

                        return stream.readAllBytes();
                        //return new byte[0];
                    } else {
                        System.err.println("Error running RML mapper. Exit code: " + exitCode2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Yarrrml parser run successfully!");
            } else {
                throw new RuntimeException("Error running Yarrrml parser. Exit code: " + exitCode);
                //System.err.println("Error running Yarrrml parser. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return new byte[0];
    }

    public static byte[] generateLinkedData(MultipartFile yamlData, MultipartFile csvData) throws IOException {
        File csvFile = new File("C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\\static\\mappings.csv");

        File yamlFile = new File("C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\\static\\mappings.yarrrml.yml");

        yamlData.transferTo(yamlFile);
        csvData.transferTo(csvFile);

        return executeYARRRMLParser();
    }
}
