package com.ttl.userportal.controller;

import com.ttl.userportal.config.CurrentUser;
import com.ttl.userportal.dto.AppraisalManagerReviewDTO;
import com.ttl.userportal.dto.AppraisalQuestionDTO;
import com.ttl.userportal.dto.AppraisalTemplateDTO;
import com.ttl.userportal.dto.EmployeeSelfAssessmentDTO;
import com.ttl.userportal.entity.ManagerAppraisalReview;
import com.ttl.userportal.service.AppraisalService;
import com.ttl.userportal.util.constants.UserPortalConstants;
import com.ttl.userportal.util.model.UserDetails;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/appraisal")
@CrossOrigin(origins = "http://localhost:3000")
public class AppraisalController {
    @Autowired
    private AppraisalService appraisalService;

    @GetMapping("/question/list")
    public ResponseEntity<Map<String, Object>> getQuestions(@CurrentUser UserDetails userDetails){
        Map<String, Object> response = new HashMap<>();

        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FETCHED_SUCCESS_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getQuestion(userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/question/getById")
    public ResponseEntity<Map<String, Object>> getQuestionById(@RequestParam Long questionId, @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FETCHED_SUCCESS_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getQuestionById(questionId, userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/question/create")
    public ResponseEntity<Map<String, Object>> createQuestion(@RequestBody AppraisalQuestionDTO appraisalQuestionDTO,
                                                              @CurrentUser UserDetails userDetails){
        Map<String, Object> response = new HashMap<>();
        try{
            appraisalService.saveOrUpdateQuestion(appraisalQuestionDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.CREATED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @PutMapping("/question/edit")
    public ResponseEntity<Map<String, Object>> updateQuestion(@RequestBody AppraisalQuestionDTO appraisalQuestionDTO,
                                                              @CurrentUser UserDetails userDetails){
        Map<String, Object> response = new HashMap<>();
        try {
            appraisalService.saveOrUpdateQuestion(appraisalQuestionDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.UPDATED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/question/delete")
    public ResponseEntity<Map<String, Object>> deleteQuestion(@RequestParam Long questionId,
                                                              @CurrentUser UserDetails userDetails){
        Map<String, Object> response = new HashMap<>();
        try {
            appraisalService.deleteQuestion(questionId, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.DELETED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/template/list")
    public ResponseEntity<Map<String, Object>> getTemplates(@CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FETCHED_SUCCESS_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getTemplates(userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/template/getById")
    public ResponseEntity<Map<String, Object>> getTemplateById(@RequestParam Long templateId,
                                                               @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        log.error("Authorities: {}",
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities());
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FETCHED_SUCCESS_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getTemplateById(templateId, userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/template/create")
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody AppraisalTemplateDTO appraisalTemplateDTO,
                                                              @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            appraisalService.saveOrUpdateAppraisalTemplate(appraisalTemplateDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.CREATED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/template/edit")
    public ResponseEntity<Map<String, Object>> updateTemplate(@RequestBody AppraisalTemplateDTO appraisalTemplateDTO,
                                                              @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            appraisalService.saveOrUpdateAppraisalTemplate(appraisalTemplateDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.UPDATED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/template/delete")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@RequestParam Long templateId,
                                                              @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();

        try {
            appraisalService.deleteAppraisalTemplate(templateId, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.DELETED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/self-assessment/list")
    public ResponseEntity<Map<String, Object>> getSelfAssessmentList(@CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FETCHED_SUCCESS_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getEmployeeSelfAssessmentList(userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/self-assessment/getById")
    public ResponseEntity<Map<String, Object>> getSelfAssessment(@RequestParam Long templateId, @CurrentUser UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FETCHED_SUCCESS_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getEmployeeSelfAssessment(templateId, userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/self-assessment/save")
    public ResponseEntity<Map<String, Object>> saveSelfAssessment(
            @RequestBody EmployeeSelfAssessmentDTO employeeSelfAssessmentDTO, @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            appraisalService.saveOrUpdateEmployeeAnswers(employeeSelfAssessmentDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.CREATED_SUCCESSFULLY);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/self-assessment/update")
    public ResponseEntity<Map<String, Object>> updateSelfAssessment(
            @RequestBody EmployeeSelfAssessmentDTO employeeSelfAssessmentDTO, @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            appraisalService.saveOrUpdateEmployeeAnswers(employeeSelfAssessmentDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.UPDATED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/self-assessment/delete")
    public ResponseEntity<Map<String, Object>> deleteSelfAssessment(@RequestParam Long answerId,
                                                                    @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            appraisalService.softDeleteAnswer(answerId, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.DELETED_SUCCESSFULLY);
            response.put(UserPortalConstants.DATA, "");
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/self-appraisal-review/list")
    public ResponseEntity<Map<String, Object>> getListSelfAppraisalReview(@CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getTeamSelfAssessments(userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/self-assessment-review/getById")
    public ResponseEntity<Map<String, Object>> getSelfAppraisalReview(
            @RequestParam Long templateId,
            @RequestParam Long employeeId,
            @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getSelfAssessmentForReview(templateId, employeeId, userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/self-assessment-review/getForEmployee")
    public ResponseEntity<Map<String, Object>> getEmployeeSelfAppraisalReview(@RequestParam Long templateId,
                                                                              @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            response.put(UserPortalConstants.DATA, appraisalService.getEmployeeSelfAssessmentReview(templateId, userDetails));
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/self-assessment-review/save")
    public ResponseEntity<Map<String, Object>> saveSelfAssessmentReview(
            @RequestBody AppraisalManagerReviewDTO appraisalManagerReviewDTO, @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            appraisalService.saveOrUpdateSelfAssessmentReview(appraisalManagerReviewDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.CREATED_SUCCESSFULLY);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/self-assessment-review/update")
    public ResponseEntity<Map<String, Object>> updateSelfAssessmentReview(
            @RequestBody AppraisalManagerReviewDTO appraisalManagerReviewDTO, @CurrentUser UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            appraisalService.saveOrUpdateSelfAssessmentReview(appraisalManagerReviewDTO, userDetails);
            response.put(UserPortalConstants.HAS_ERROR, Boolean.FALSE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.UPDATED_SUCCESSFULLY);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put(UserPortalConstants.HAS_ERROR, Boolean.TRUE);
            response.put(UserPortalConstants.MESSAGE, UserPortalConstants.FAILURE_MESSAGE);
            return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}