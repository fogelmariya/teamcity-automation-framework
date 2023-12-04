package com.example.teamcity.api;

import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.RandomData;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.models.BuildType;
import com.example.teamcity.api.requests.checked.CheckedBuildConfig;
import com.example.teamcity.api.requests.checked.CheckedProject;
import com.example.teamcity.api.requests.checked.CheckedUser;
import com.example.teamcity.api.requests.unchecked.UncheckedBuildConfig;
import com.example.teamcity.api.requests.unchecked.UncheckedProject;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

public class BuildConfigurationTest extends BaseApiTest {

    @Test
    public void buildConfigurationTest() {
        var testData = testDataStorage.addTestData();

        new CheckedUser(Specifications.getSpec().superUserSpec()).create(testData.getUser());
        var project = new CheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getProject());

        softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());
    }

    @Test
    public void projectDeveloperDontHaveRightsToCreateBuildConfig() {
        var testData = testDataStorage.addTestData();
        testData.getUser().setRoles(TestDataGenerator
                .generateRoles(Role.PROJECT_DEVELOPER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void projectViewerDontHaveRightsToCreateBuildConfig() {
        var testData = testDataStorage.addTestData();
        testData.getUser().setRoles(TestDataGenerator
                .generateRoles(Role.PROJECT_VIEWER, "g"));

        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void agentManagerDontHaveRightsToCreateBuildConfig() {
        var testData = testDataStorage.addTestData();
        testData.getUser().setRoles(TestDataGenerator
                .generateRoles(Role.AGENT_MANAGER, "g"));
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void createBuildConfigWithLongName() {
        var testData = testDataStorage.addTestData();
        String newString = "a".repeat(21474);
        testData.getBuildType().setName(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        var buildType = new CheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType());

        softy.assertThat(buildType.getId()).isEqualTo(testData.getBuildType().getId());
    }

    @Test
    public void createBuildConfigWithEmptyName() {
        var testData = testDataStorage.addTestData();
        String newString = "";
        testData.getBuildType().setName(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString().contains("Project name cannot be empty.");
    }

    @Test
    public void createBuildConfigNameWithOnlySpecialSymbols() {
        var testData = testDataStorage.addTestData();
        String newString = "_ \\'?.{}@.!$%^&*()";
        testData.getBuildType().setName(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        var buildType = new CheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType());

        softy.assertThat(buildType.getName()).isEqualTo(newString);
    }

    @Test
    public void buildConfigsCantHaveSameNames() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        var project = new CheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType());

        softy.assertThat(project.getId()).isEqualTo(testData.getBuildType().getId());

        String project1 = new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString();

        softy.assertThat(project1.contains("Project with this name already exists: "));
    }

    @Test
    public void createBuildConfigWithEmptyID() {
        var testData = testDataStorage.addTestData();
        String newString = "";
        testData.getBuildType().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("Project ID must not be empty");
    }

    @Test
    public void buildConfigWithLongIdCantBeCreated() {
        var testData = testDataStorage.addTestData();
        String newString = "a".repeat(226);
        testData.getBuildType().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("the maximum length is 225");
    }

    @Test
    public void createBuildConfigIdWithOnlySpecialSymbols() {
        var testData = TestDataGenerator.generate();
        String newString = "_.{}@.!$%^&*()";
        testData.getBuildType().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        new UncheckedProject(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("starts with non-letter character");
    }

    @Test
    public void createBuildConfigIdStartsWithNonLetterSymbols() {
        var testData = TestDataGenerator.generate();
        String newString = "1 \\'?@123";
        testData.getBuildType().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("starts with non-letter character");
    }

    @Test
    public void buildConfigIdWithSpecialSymbolsCantBeCreated() {
        var testData = TestDataGenerator.generate();
        String newString = "a123@.!$%^&*(){}";
        testData.getBuildType().setId(newString);
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .extract().asString().contains("contain only latin letters, digits and underscores");
    }

    @Test
    public void buildConfigsCantHaveSameIds() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());
        var buildConfig1 = new CheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType());

        softy.assertThat(buildConfig1.getId()).isEqualTo(testData.getBuildType().getId());

        testData.getBuildType().setName("newName");
        String buildConfig2 = new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(testData.getBuildType())
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString();

        softy.assertThat(buildConfig2.contains("Project ID \"" + testData.getProject().getId()+ "\" is already used by another project"));
    }

    @Test
    public void buildConfigShouldHaveName() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        var buildConfig = BuildType.builder()
                .id(RandomData.getString())
                .project(testData.getProject())
                .build();

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(buildConfig)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString().contains("Project name cannot be empty.");
    }

    @Test
    public void buildConfigCouldBeCreatedWithoutId() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        var buildConfig = BuildType.builder()
                .name(RandomData.getString())
                .project(testData.getProject())
                .build();

        new CheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(buildConfig);
    }

    @Test
    public void buildConfigShouldHaveProject() {
        var testData = testDataStorage.addTestData();
        checkedWithSuperUser.getUserRequest().create(testData.getUser());
        checkedWithSuperUser.getProjectRequest().create(testData.getProject());

        var buildConfig = BuildType.builder()
                .id(RandomData.getString())
                .name(RandomData.getString())
                .build();

        new UncheckedBuildConfig(Specifications.getSpec()
                .authSpec(testData.getUser()))
                .create(buildConfig)
                .then().assertThat().statusCode(HttpStatus.SC_BAD_REQUEST)
                .extract().asString().contains("Project name cannot be empty.");
    }
}
