package com.ttl.userportal.service;

import com.ttl.userportal.dto.AppraisalQuestionDTO;
import com.ttl.userportal.dto.AppraisalTemplateDTO;
import com.ttl.userportal.dto.EmployeeAnswerDTO;
import com.ttl.userportal.dto.EmployeeSelfAssessmentDTO;
import com.ttl.userportal.entity.AppraisalQuestions;
import com.ttl.userportal.entity.AppraisalTemplate;
import com.ttl.userportal.entity.AppraisalTemplateQuestion;
import com.ttl.userportal.entity.EmployeeAppraisalAnswer;
import com.ttl.userportal.repository.AppraisalQuestionRepository;
import com.ttl.userportal.repository.AppraisalTemplateQuestionRepository;
import com.ttl.userportal.repository.AppraisalTemplateRepository;
import com.ttl.userportal.repository.EmployeeAppraisalAnswerRepository;
import com.ttl.userportal.util.constants.UserPortalConstants;
import com.ttl.userportal.util.model.UserDetails;
import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AppraisalService {
    @Autowired
    private AppraisalQuestionRepository appraisalQuestionRepository;

    @Autowired
    private AppraisalTemplateQuestionRepository appraisalTemplateQuestionRepository;

    @Autowired
    private AppraisalTemplateRepository appraisalTemplateRepository;

    @Autowired
    private EmployeeAppraisalAnswerRepository employeeAppraisalAnswerRepository;

    @Transactional
    public  List<AppraisalQuestionDTO> getQuestion(UserDetails userDetails){
        List<AppraisalQuestions> appraisalQuestions = appraisalQuestionRepository.findAllByRecordStatus(true);
        List<AppraisalQuestionDTO> appraisalQuestionDTOS = new ArrayList<>();
        for (AppraisalQuestions appraisalQuestion : appraisalQuestions){
            AppraisalQuestionDTO appraisalQuestionDTO = new AppraisalQuestionDTO();
            appraisalQuestionDTO.setId(appraisalQuestion.getId());
            appraisalQuestionDTO.setQuestionText(appraisalQuestion.getQuestionText());
            appraisalQuestionDTOS.add(appraisalQuestionDTO);
        }
        return appraisalQuestionDTOS;
    }

    @Transactional
    public AppraisalQuestionDTO getQuestionById(Long questionId, UserDetails userDetails) {
        AppraisalQuestions appraisalQuestions = appraisalQuestionRepository.findByIdAndRecordStatus(questionId, true)
                .orElseThrow(()-> new RuntimeException("Question not exist"));

        AppraisalQuestionDTO appraisalQuestionDTO = new AppraisalQuestionDTO();
        appraisalQuestionDTO.setId(appraisalQuestions.getId());
        appraisalQuestionDTO.setQuestionText(appraisalQuestions.getQuestionText());

        return appraisalQuestionDTO;
    }

    @Transactional
    public void saveOrUpdateQuestion(AppraisalQuestionDTO appraisalQuestionDTO, UserDetails userDetails){
        if(StringUtils.isEmpty(appraisalQuestionDTO.getQuestionText())){
            throw new IllegalArgumentException(UserPortalConstants.QUESTION_IS_MANDATORY);
        }

        String questionText = appraisalQuestionDTO.getQuestionText().trim();

        if(appraisalQuestionDTO.getId() == null){

            if(appraisalQuestionRepository.existsByQuestionTextIgnoreCaseAndRecordStatusTrue(questionText)) {
                throw new IllegalArgumentException(UserPortalConstants.QUESTION_ALREADY_EXIST);
            }

            AppraisalQuestions appraisalQuestions = AppraisalQuestions.builder()
                    .questionText(questionText)
                    .recordStatus(Boolean.TRUE)
                    .createdBy(userDetails.getFirst_name())
                    .createdAt(LocalDateTime.now())
                    .build();

            appraisalQuestionRepository.save(appraisalQuestions);
        }else
        {
            AppraisalQuestions existingAppraisalQuestions =
                    appraisalQuestionRepository.findByIdAndRecordStatus(appraisalQuestionDTO.getId(), true)
                            .orElseThrow(()-> new RuntimeException("Question not found"));

            if(appraisalQuestionRepository.existsDuplicateForUpdate(questionText, appraisalQuestionDTO.getId())) {
                throw new IllegalArgumentException("Question Already Exist");
            }

            existingAppraisalQuestions.setQuestionText(appraisalQuestionDTO.getQuestionText());
            existingAppraisalQuestions.setUpdatedBy(userDetails.getFirst_name());
            existingAppraisalQuestions.setUpdatedAt(LocalDateTime.now());
            appraisalQuestionRepository.save(existingAppraisalQuestions);
        }
    }

    @Transactional
    public void deleteQuestion(Long questionId, UserDetails userDetails){
        AppraisalQuestions appraisalQuestions = appraisalQuestionRepository.findByIdAndRecordStatus(questionId, true)
                .orElseThrow(()-> new RuntimeException("Question not found"));
        appraisalQuestions.setRecordStatus(Boolean.FALSE);
        appraisalQuestions.setUpdatedAt(LocalDateTime.now());
        appraisalQuestions.setUpdatedBy(userDetails.getFirst_name());
        appraisalQuestionRepository.save(appraisalQuestions);

        List<AppraisalTemplateQuestion> appraisalTemplateQuestions = appraisalTemplateQuestionRepository
                .findAllByQuestionIdAndRecordStatus(questionId, true);
        for(AppraisalTemplateQuestion appraisalTemplateQuestion : appraisalTemplateQuestions) {
            appraisalTemplateQuestion.setRecordStatus(Boolean.FALSE);
            appraisalTemplateQuestionRepository.save(appraisalTemplateQuestion);
        }

        List<EmployeeAppraisalAnswer> answers =
                employeeAppraisalAnswerRepository
                        .findAllByQuestionIdAndRecordStatus(questionId, true);

        for (EmployeeAppraisalAnswer answer : answers) {
            answer.setRecordStatus(false);
            answer.setUpdatedBy(userDetails.getFirst_name());
            answer.setUpdatedAt(LocalDateTime.now());
            employeeAppraisalAnswerRepository.save(answer);
        }
    }

    @Transactional
    public List<AppraisalTemplateDTO> getTemplates(UserDetails userDetails) {
        List<AppraisalTemplate> appraisalTemplates = appraisalTemplateRepository.findAllByRecordStatus(true);
        List<AppraisalTemplateDTO> appraisalTemplateDTOs = new ArrayList<>();

        for (AppraisalTemplate appraisalTemplate : appraisalTemplates) {
            AppraisalTemplateDTO appraisalTemplateDTO = new AppraisalTemplateDTO();
            appraisalTemplateDTO.setTemplateId(appraisalTemplate.getId());
            appraisalTemplateDTO.setTemplateName(appraisalTemplate.getTemplateName());
            appraisalTemplateDTOs.add(appraisalTemplateDTO);
        }

        return appraisalTemplateDTOs;
    }

    @Transactional
    public AppraisalTemplateDTO getTemplateById(Long templateId, UserDetails userDetails) {

        AppraisalTemplate template =
                appraisalTemplateRepository.findByIdAndRecordStatus(templateId, true)
                        .orElseThrow(() -> new RuntimeException("Template not found"));

        List<AppraisalTemplateQuestion> mappings =
                appraisalTemplateQuestionRepository
                        .findAllByTemplateIdAndRecordStatus(templateId, true);

        List<AppraisalQuestionDTO> questions = new ArrayList<>();

        for (AppraisalTemplateQuestion mapping : mappings) {
            AppraisalQuestions question =
                    appraisalQuestionRepository
                            .findByIdAndRecordStatus(mapping.getQuestionId(), true)
                            .orElseThrow(() -> new RuntimeException("Question not found"));

            AppraisalQuestionDTO qDto = new AppraisalQuestionDTO();
            qDto.setId(question.getId());
            qDto.setQuestionText(question.getQuestionText());
            questions.add(qDto);
        }

        AppraisalTemplateDTO dto = new AppraisalTemplateDTO();
        dto.setTemplateId(template.getId());
        dto.setTemplateName(template.getTemplateName());
        dto.setAppraisalQuestions(questions);

        return dto;
    }

    @Transactional
    public void saveOrUpdateAppraisalTemplate(AppraisalTemplateDTO appraisalTemplateDTO, UserDetails userDetails) {

        AppraisalTemplate appraisalTemplate;
        AppraisalTemplateQuestion appraisalTemplateQuestion;

        if(appraisalTemplateDTO.getTemplateId() == null) {
            appraisalTemplate = AppraisalTemplate.builder()
                    .templateName(appraisalTemplateDTO.getTemplateName())
                    .recordStatus(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            appraisalTemplateRepository.save(appraisalTemplate);
        }
        else {
            appraisalTemplate = appraisalTemplateRepository.findById(appraisalTemplateDTO.getTemplateId())
                    .orElseThrow(()->new RuntimeException("Template not found"));

            appraisalTemplate.setTemplateName(appraisalTemplateDTO.getTemplateName());
            appraisalTemplate.setUpdatedBy(userDetails.getFirst_name());
            appraisalTemplate.setUpdatedAt(LocalDateTime.now());

            appraisalTemplateRepository.save(appraisalTemplate);
        }

        Long templateId = appraisalTemplate.getId();

        // existing mappings
        List<AppraisalTemplateQuestion> existingMappings =
                appraisalTemplateQuestionRepository
                        .findAllByTemplateIdAndRecordStatus(templateId, true);

        Set<Long> existingQuestionIds = existingMappings.stream()
                .map(AppraisalTemplateQuestion::getQuestionId)
                .collect(Collectors.toSet());

        Set<Long> incomingQuestionIds = new HashSet<>();

        for (AppraisalQuestionDTO qDto : appraisalTemplateDTO.getAppraisalQuestions()) {

            incomingQuestionIds.add(qDto.getId());

            AppraisalQuestions existingQuestion =
                    appraisalQuestionRepository
                            .findByIdAndRecordStatus(qDto.getId(), true)
                            .orElseThrow(() -> new RuntimeException("Question not found"));

            linkTemplateQuestion(templateId, qDto.getId());
        }

        for(Long existingQId : existingQuestionIds) {
           if(!incomingQuestionIds.contains(existingQId)) {
               deleteAppraisalTemplateQuestion(templateId, existingQId, userDetails);
           }
        }
    }

    public void linkTemplateQuestion(Long templateId, Long questionId) {

        Optional<AppraisalTemplateQuestion> optional = appraisalTemplateQuestionRepository
                .findByTemplateIdAndQuestionId(templateId, questionId);

        if(optional.isPresent()) {
            AppraisalTemplateQuestion appraisalTemplateQuestion = optional.get();

            if(!Boolean.TRUE.equals(appraisalTemplateQuestion.getRecordStatus())) {
              appraisalTemplateQuestion.setRecordStatus(Boolean.TRUE);
              appraisalTemplateQuestion.setCreatedAt(LocalDateTime.now());
              appraisalTemplateQuestionRepository.save(appraisalTemplateQuestion);
            }

        } else {
            AppraisalTemplateQuestion appraisalTemplateQuestion = AppraisalTemplateQuestion.builder()
                    .templateId(templateId)
                    .questionId(questionId)
                    .recordStatus(Boolean.TRUE)
                    .createdAt(LocalDateTime.now())
                    .build();

            appraisalTemplateQuestionRepository.save(appraisalTemplateQuestion);
        }
    }

    @Transactional
    public void deleteAppraisalTemplate(Long templateId, UserDetails userDetails) {
        AppraisalTemplate appraisalTemplate = appraisalTemplateRepository
                .findByIdAndRecordStatus(templateId, true).orElseThrow(()->new RuntimeException("Template not found"));

        appraisalTemplate.setRecordStatus(Boolean.FALSE);
        appraisalTemplate.setUpdatedBy(userDetails.getFirst_name());
        appraisalTemplate.setUpdatedAt(LocalDateTime.now());
        appraisalTemplateRepository.save(appraisalTemplate);

        List<AppraisalTemplateQuestion> appraisalTemplateQuestions = appraisalTemplateQuestionRepository
                .findAllByTemplateIdAndRecordStatus(templateId, true);

        for(AppraisalTemplateQuestion appraisalTemplateQuestion : appraisalTemplateQuestions){
            deleteAppraisalTemplateQuestion(appraisalTemplateQuestion.getTemplateId(), appraisalTemplateQuestion.getQuestionId(), userDetails);
        }
    }

    @Transactional
    public void deleteAppraisalTemplateQuestion(Long templateId, Long questionId, UserDetails userDetails) {
        appraisalTemplateQuestionRepository
                .findByTemplateIdAndQuestionIdAndRecordStatus(templateId, questionId, true)
                .ifPresent( appraisalTemplateQuestion -> {
                    appraisalTemplateQuestion.setRecordStatus(false);
                    appraisalTemplateQuestionRepository.save(appraisalTemplateQuestion);
                });
    }

    @Transactional(readOnly = true)
    public EmployeeSelfAssessmentDTO getEmployeeSelfAssessment(
            Long templateId,
            UserDetails userDetails) {

        Long userId = userDetails.getUser_id();

        // 1. Validate template
        AppraisalTemplate template =
                appraisalTemplateRepository
                        .findByIdAndRecordStatus(templateId, true)
                        .orElseThrow(() -> new RuntimeException("Template not found"));

        // 2. Fetch template-question mappings
        List<AppraisalTemplateQuestion> mappings =
                appraisalTemplateQuestionRepository
                        .findAllByTemplateIdAndRecordStatus(templateId, true);

        List<EmployeeAnswerDTO> questionAnswerList = new ArrayList<>();

        for (AppraisalTemplateQuestion mapping : mappings) {

            // 3. Fetch question
            AppraisalQuestions question =
                    appraisalQuestionRepository
                            .findByIdAndRecordStatus(mapping.getQuestionId(), true)
                            .orElseThrow(() -> new RuntimeException("Question not found"));

            // 4. Fetch answer (if exists)
            Optional<EmployeeAppraisalAnswer> answerOpt =
                    employeeAppraisalAnswerRepository
                            .findByTemplateIdAndQuestionIdAndUserId(
                                    templateId,
                                    question.getId(),
                                    userId
                            );

            EmployeeAnswerDTO dto = new EmployeeAnswerDTO();
            dto.setQuestionId(question.getId());
            dto.setQuestionText(question.getQuestionText());

            if (answerOpt.isPresent()
                    && Boolean.TRUE.equals(answerOpt.get().getRecordStatus())) {

                dto.setAnswerId(answerOpt.get().getId());
                dto.setAnswerText(answerOpt.get().getAnswerText());
            }

            questionAnswerList.add(dto);
        }

        EmployeeSelfAssessmentDTO response = new EmployeeSelfAssessmentDTO();
        response.setTemplateId(template.getId());
        response.setTemplateName(template.getTemplateName());
        response.setQuestionAnswers(questionAnswerList);

        return response;
    }

    @Transactional
    public void saveOrUpdateEmployeeAnswers(
            EmployeeSelfAssessmentDTO dto,
            UserDetails userDetails) {

        Long userId = userDetails.getUser_id();

        for (EmployeeAnswerDTO qa : dto.getQuestionAnswers()) {

            appraisalTemplateQuestionRepository
                    .findByTemplateIdAndQuestionIdAndRecordStatus(
                            dto.getTemplateId(),
                            qa.getQuestionId(),
                            true
                    )
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Question is not the part of this template"
                            )
                    );

            appraisalQuestionRepository
                    .findByIdAndRecordStatus(qa.getQuestionId(), true)
                    .orElseThrow(() ->
                            new RuntimeException("Question is deleted")
                    );

            EmployeeAppraisalAnswer answer =
                    employeeAppraisalAnswerRepository
                            .findByTemplateIdAndQuestionIdAndUserId(
                                    dto.getTemplateId(),
                                    qa.getQuestionId(),
                                    userId
                            )
                            .orElse(null);

            if (answer == null) {
                answer = new EmployeeAppraisalAnswer();
                answer.setTemplateId(dto.getTemplateId());
                answer.setQuestionId(qa.getQuestionId());
                answer.setAnswerText(qa.getAnswerText());
                answer.setUserId(userId);
                answer.setCreatedBy(userDetails.getFirst_name());
                answer.setCreatedAt(LocalDateTime.now());
                answer.setRecordStatus(true);
            }else{
                answer.setAnswerText(qa.getAnswerText());
                answer.setUpdatedBy(userDetails.getFirst_name());
                answer.setUpdatedAt(LocalDateTime.now());
            }


            employeeAppraisalAnswerRepository.save(answer);
        }
    }

    @Transactional
    public void softDeleteAnswer(Long answerId, UserDetails userDetails) {

        EmployeeAppraisalAnswer answer =
                employeeAppraisalAnswerRepository.findById(answerId)
                        .orElseThrow(() -> new RuntimeException("Answer not found"));

        if (!answer.getUserId().equals(userDetails.getUser_id())) {
            throw new RuntimeException("Unauthorized");
        }

        answer.setRecordStatus(false);
        answer.setUpdatedBy(userDetails.getFirst_name());
        answer.setUpdatedAt(LocalDateTime.now());

        employeeAppraisalAnswerRepository.save(answer);
    }


}
