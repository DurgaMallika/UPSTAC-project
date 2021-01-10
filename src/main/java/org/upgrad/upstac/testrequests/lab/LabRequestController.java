package org.upgrad.upstac.testrequests.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;

@RestController
@RequestMapping("/api/labrequests")
public class LabRequestController {

  Logger log = LoggerFactory.getLogger(LabRequestController.class);

  @Autowired private TestRequestUpdateService testRequestUpdateService;

  @Autowired private TestRequestQueryService testRequestQueryService;

  @Autowired private TestRequestFlowService testRequestFlowService;

  @Autowired private UserLoggedInService userLoggedInService;

  @GetMapping("/to-be-tested")
  @PreAuthorize("hasAnyRole('TESTER')")
  // This method will return all the requests with the request status INITIATED
  public List<TestRequest> getForTests() {
    return testRequestQueryService.findBy(RequestStatus.INITIATED);
  }

  @GetMapping
  @PreAuthorize("hasAnyRole('TESTER')")
  // This method will return all the requests assigned for the particular logged in tester
  public List<TestRequest> getForTester() {
    User tester = userLoggedInService.getLoggedInUser();
    return testRequestQueryService.findByTester(tester);
  }

  @PreAuthorize("hasAnyRole('TESTER')")
  @PutMapping("/assign/{id}")
  // This method will assign the selected test requests to the logged in tester
  public TestRequest assignForLabTest(@PathVariable Long id) {
    User tester = userLoggedInService.getLoggedInUser();
    return testRequestUpdateService.assignForLabTest(id, tester);
  }

  @PreAuthorize("hasAnyRole('TESTER')")
  @PutMapping("/update/{id}")
  //This method will update the result of the lab test in data base
  public TestRequest updateLabTest(
      @PathVariable Long id, @RequestBody CreateLabResult createLabResult) {

    try {
      User tester = userLoggedInService.getLoggedInUser();
      return testRequestUpdateService.updateLabTest(id, createLabResult, tester);
    } catch (ConstraintViolationException e) {
      throw asConstraintViolation(e);
    } catch (AppException e) {
      throw asBadRequest(e.getMessage());
    }
  }
}
