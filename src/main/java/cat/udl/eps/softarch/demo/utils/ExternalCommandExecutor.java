package cat.udl.eps.softarch.demo.utils;

import java.io.File;
import java.io.IOException;

public class ExternalCommandExecutor {
    public void executeYARRRMLParser() {

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
                builder2.redirectOutput(new File(
                        "C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\\static\\output.txt"));

                try {
                    Process process2 = builder2.start();
                    process2.waitFor(); // Wait for the container to finish
                    int exitCode2 = process.exitValue();

                    if (exitCode2 == 0) {
                        System.out.println("RML mapper run successfully!");
                    } else {
                        System.err.println("Error running RML mapper. Exit code: " + exitCode2);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Yarrrml parser run successfully!");
            } else {
                System.err.println("Error running Yarrrml parser. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }


//        ProcessBuilder builder2 = new ProcessBuilder("docker", "container", "run", "--rm", "--name", "yarrrmlmapper",
//                "-v", "C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\\resources:/mnt/data",
//                "yarrrmlmapper", "mappings.yarrrml.yml");
//
//        // Redirect standard output (STDOUT) to a file
//        builder2.redirectOutput(new File(
//                "C:\\Users\\Zihan\\Desktop\\TFG\\tab2kgwiz-api\\src\\main\\resources\\output.txt"));
//
//        try {
//            Process process = builder2.start();
//            process.waitFor(); // Wait for the container to finish
//            int exitCode = process.exitValue();
//
//            if (exitCode == 0) {
//                System.out.println("Docker container run successfully!");
//            } else {
//                System.err.println("Error running Docker container. Exit code: " + exitCode);
//            }
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }

    }
}
