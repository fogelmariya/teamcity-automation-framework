package com.example.teamcity.api;

import com.example.teamcity.api.enums.Role;
import com.example.teamcity.api.generators.TestDataGenerator;
import com.example.teamcity.api.requests.CheckedRequests;
import com.example.teamcity.api.requests.checked.CheckedBuildConfig;
import com.example.teamcity.api.requests.checked.CheckedProject;
import com.example.teamcity.api.requests.unchecked.UncheckedBuildConfig;
import com.example.teamcity.api.spec.Specifications;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

public class RolesTest extends BaseApiTest {

    @Test
    public void unauthorizedUserShouldNotHaveRightToCreateProject() {
        var testData = testDataStorage.addTestData();

        uncheckedWithSuperUser.getProjectRequest()
                .create(testData.getProject())
                .then().assertThat().statusCode(HttpStatus.SC_UNAUTHORIZED)
                .body(Matchers.containsString("Authentication required"));
        uncheckedWithSuperUser.getProjectRequest()
                .get(testData.getProject().getId())
                .then().assertThat().statusCode(org.apache.hc.core5.http.HttpStatus.SC_NOT_FOUND)
                .body(Matchers.containsString("No project found by locator 'count:1,id:" + testData.getProject().getId()));
    }

   @Test
    public void systemAdminShouldHaveRightsToCreateProject() {
       var testData = testDataStorage.addTestData();

       testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.SYSTEM_ADMIN, "g"));

       checkedWithSuperUser.getUserRequest()
                .create(testData.getUser());
        var project = new CheckedProject(Specifications.getSpec()
               .authSpec(testData.getUser()))
               .create(testData.getProject());

       softy.assertThat(project.getId()).isEqualTo(testData.getProject().getId());

   }

   @Test
    public void projectAdminShouldHaveRightsToCreateBuildConfigToHisProject() {
       var testData = testDataStorage.addTestData();

       checkedWithSuperUser.getProjectRequest()
               .create(testData.getProject());
       testData.getUser().setRoles(TestDataGenerator.generateRoles(Role.PROJECT_ADMIN, "p:" + testData.getProject().getId()));

       checkedWithSuperUser.getUserRequest()
               .create(testData.getUser());

       var buildConfig = new CheckedBuildConfig(Specifications.getSpec().authSpec(testData.getUser()))
               .create(testData.getBuildType());

       softy.assertThat(buildConfig.getId()).isEqualTo(testData.getBuildType().getId());
    }

   @Test
    public void projectAdminShouldNotHaveRightsToCreateBuildConfigToAnotherProject() {
       var firsttestData = testDataStorage.addTestData();
       var secondtestData = testDataStorage.addTestData();
       var firstUserRequest = new CheckedRequests(Specifications.getSpec().authSpec(firsttestData.getUser()));
       var secondUserRequest = new CheckedRequests(Specifications.getSpec().authSpec(secondtestData.getUser()));

       checkedWithSuperUser.getProjectRequest().create(firsttestData.getProject());
       checkedWithSuperUser.getProjectRequest().create(secondtestData.getProject());
       firsttestData.getUser().setRoles(TestDataGenerator
               .generateRoles(Role.PROJECT_ADMIN, "p:" + firsttestData.getProject().getId()));

       checkedWithSuperUser.getUserRequest().create(firsttestData.getUser());

       secondtestData.getUser().setRoles(TestDataGenerator
               .generateRoles(Role.PROJECT_ADMIN, "p:" + secondtestData.getProject().getId()));

       checkedWithSuperUser.getUserRequest().create(secondtestData.getUser());
       var buildConfig = new UncheckedBuildConfig(Specifications.getSpec().authSpec(secondtestData.getUser()))
               .create(firsttestData.getBuildType())
               .then().assertThat().statusCode(HttpStatus.SC_FORBIDDEN);

//       softy.assertThat(buildConfig.getId()).isEqualTo(firsttestData.getBuildType().getId());

   }
}
