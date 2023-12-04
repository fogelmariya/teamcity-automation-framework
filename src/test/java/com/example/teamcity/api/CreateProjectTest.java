package com.example.teamcity.api;

import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.NewProjectDescription;
import com.example.teamcity.api.models.Project;
import com.example.teamcity.api.requests.checked.CheckedProject;
import com.example.teamcity.api.requests.checked.CheckedUser;
import com.example.teamcity.api.requests.unchecked.UncheckedProject;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

public class CreateProjectTest extends BaseApiTest{

    @Test
    public void createProject() {
        var testData = testDataStorage.addTestData();

        new CheckedUser(Specifications.getSpec().superUserSpec()).create(testData.getUser());
        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());
    }

    @Test
    public void projectViewerShouldNotHaveRightsToCreateProject() {
        var testData = testDataStorage.addTestData();
        testData.getUser().setRoles(TestDataGenerator
                .generateRoles(Role.PROJECT_VIEWER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void projectDeveloperShouldNotHaveRightsToCreateProject() {
        var testData = testDataStorage.addTestData();
        testData.getUser().setRoles(TestDataGenerator
                .generateRoles(Role.PROJECT_DEVELOPER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void agentManagerShouldNotHaveRightsToCreateProject() {
        var testData = testDataStorage.addTestData();
        testData.getUser().setRoles(TestDataGenerator
                .generateRoles(Role.AGENT_MANAGER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void createProjectWithLongName() {
        var testData = testDataStorage.addTestData();
        String newString = "a".repeat(21474);
        testData.getProject().setName(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());
    }

    @Test
    public void createProjectWithEmptyName() {
        var testData = testDataStorage.addTestData();
        String newString = "";
        testData.getProject().setName(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
       new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString().contains("Project name cannot be empty.");

    }

    @Test
    public void createProjectNameWithOnlySpecialSymbols() {
        var testData = testDataStorage.addTestData();
        String newString = "_ \\'?.{}@.!$%^&*()";
        testData.getProject().setName(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getName()).isEqualTo(newString);
    }

    @Test
    public void projectsCantHaveSameNames() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());

        String project1 = new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString();

        softy.assertThat(project1.contains("Project with this name already exists: "));
    }

    @Test
    public void projectShouldHaveNonEmptyParentProjectLocator() {
        var testData = testDataStorage.addTestData();
        testData.getProject().setParentProject(Project.builder()
                .locator("")
                .build());

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString().contains("No project specified. Either 'id', 'internalId' or 'locator' attribute should be present");
    }

    @Test
    public void createProjectWithEmptyID() {
        var testData = testDataStorage.addTestData();
        String newString = "";
        testData.getProject().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("Project ID must not be empty");
    }


    @Test
    public void projectWithLongIdCantBeCreated() {
        var testData = testDataStorage.addTestData();
        String newString = "a".repeat(226);
        testData.getProject().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("the maximum length is 225");
    }

    @Test
    public void createProjectIdWithOnlySpecialSymbols() {
        var testData = TestDataGenerator.generate();
        String newString = "_.{}@.!$%^&*()";
        testData.getProject().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("starts with non-letter character");
    }

    @Test
    public void createProjectIdStartsWithNonLetterSymbols() {
        var testData = TestDataGenerator.generate();
        String newString = "1 \\'?@123";
        testData.getProject().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("starts with non-letter character");
    }

    @Test
    public void projectIdWithSpecialSymbolsCantBeCreated() {
        var testData = TestDataGenerator.generate();
        String newString = "a123@.!$%^&*(){}";
        testData.getProject().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("contain only latin letters, digits and underscores");
    }

    @Test
    public void projectsCantHaveSameIds() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());

        testData.getProject().setName("newName");
        String project1 = new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString();

        softy.assertThat(project1.contains("Project ID \"" + testData.getProject().getId()+ "\" is already used by another project"));
    }

    @Test
    public void projectShouldHaveProjectName() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        var projectDescription = NewProjectDescription
                .builder()
                .parentProject(Project.builder()
                        .locator("_Root")
                        .build())
                .id(RandomData.getString())
                .copyAllAssociatedSettings(true)
                .build();

        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(projectDescription)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString().contains("Project name cannot be empty.");
    }

    @Test
    public void projectCanBeCreatedWithoutProjectId() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        var projectDescription = NewProjectDescription
                .builder()
                .parentProject(Project.builder()
                        .locator("_Root")
                        .build())
                .name(RandomData.getString())
                .copyAllAssociatedSettings(true)
                .build();

        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(projectDescription);

        softy.assertThat(project.getName().equals(testData.getProject().getName()));
    }

    @Test
    public void projectCanBeCreatedWithoutParentProject() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());

        var projectDescription = NewProjectDescription
                .builder()
                .id(RandomData.getString())
                .name(RandomData.getString())
                .build();

        new  CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(projectDescription);
    }
}
