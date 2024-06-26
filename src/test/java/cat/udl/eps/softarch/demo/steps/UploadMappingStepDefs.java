//package cat.udl.eps.softarch.demo.steps;
//
//import cat.udl.eps.softarch.demo.domain.Mapping;
//import cat.udl.eps.softarch.demo.repository.MappingRepository;
//import cat.udl.eps.softarch.demo.repository.SupplierRepository;
//import io.cucumber.java.en.And;
//import io.cucumber.java.en.Then;
//import io.cucumber.java.en.When;
//import org.springframework.core.io.Resource;
//import org.springframework.http.MediaType;
//import org.springframework.util.FileCopyUtils;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.io.ByteArrayOutputStream;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//
//public class UploadMappingStepDefs {
//    final StepDefs stepDefs;
//    final MappingRepository mappingRepository;
//    final WebApplicationContext wac;
//
//    public static Mapping mappingFile;
//
//    public UploadMappingStepDefs(StepDefs stepDefs, MappingRepository mappingRepository, WebApplicationContext wac) {
//        this.stepDefs = stepDefs;
//        this.mappingRepository = mappingRepository;
//        this.wac = wac;
//    }
//
//    @When("I upload a mapping file with title {string}")
//    public void iUploadAMappingFileWithTitle(String title) throws Exception {
//        mappingFile = mappingRepository.findByTitleContaining(title).get(0);
//        Resource file = wac.getResource("classpath:" + title);
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        FileCopyUtils.copy(file.getInputStream(), output);
//        mappingFile.setFileContent(output.toString());
//        String message = stepDefs.mapper.writeValueAsString(mappingFile);
//
//        stepDefs.result = stepDefs.mockMvc.perform(
//                patch("/mappings/{id}", mappingFile == null ? 0 : mappingFile.getId())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(message)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .with(AuthenticationStepDefs.authenticate()))
//                .andDo(print());
//    }
//
//    @Then("The mapping repository should have a file with title {string}")
//    public void theMappingRepositoryShouldHaveAFileWithTitle(String title) throws Exception {
//        stepDefs.result = stepDefs.mockMvc.perform(
//                get("/mappings/{id}", mappingFile == null ? 0 : mappingFile.getId())
//                        .accept(MediaType.APPLICATION_JSON)
//                        .with(AuthenticationStepDefs.authenticate()))
//                .andDo(print())
//                .andExpect(jsonPath("$.title").value(title));
//    }
//
//    @And("The mapping content should be {string} file content")
//    public void theMappingContentShouldBeFileContent(String title) throws Exception {
//        stepDefs.result = stepDefs.mockMvc.perform(
//                get("/mappings/{id}", mappingFile == null ? 0 : mappingFile.getId())
//                        .accept(MediaType.APPLICATION_JSON)
//                        .with(AuthenticationStepDefs.authenticate()))
//                .andDo(print())
//                .andExpect(jsonPath("$.title", is(title)))
//                .andExpect(jsonPath("$.fileContent").value(mappingFile.getFileContent()));
//    }
//
//    @When("I upload a mapping file with title {string} with prefixes {string} and main ontology {string}")
//    public void iUploadAMappingFileWithTitleWithPrefixes(String fileName, String prefixes, String mainOntology) throws Exception {
//        mappingFile = mappingRepository.findByTitleContaining(fileName).get(0);
//        Resource file = wac.getResource("classpath:" + fileName);
//
//        mappingFile.setFileName(fileName);
//        mappingFile.setFileFormat(fileName.substring(fileName.indexOf(".") + 1));
//
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        FileCopyUtils.copy(file.getInputStream(), output);
//        mappingFile.setFileContent(output.toString());
//        mappingFile.setPrefixesURIS(prefixes);
//        mappingFile.setMainOntology(mainOntology);
//        String message = stepDefs.mapper.writeValueAsString(mappingFile);
//
//        stepDefs.result = stepDefs.mockMvc.perform(
//                        patch("/mappings/{id}", mappingFile == null ? 0 : mappingFile.getId())
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(message)
//                                .accept(MediaType.APPLICATION_JSON)
//                                .with(AuthenticationStepDefs.authenticate()))
//                .andDo(print());
//    }
//}
