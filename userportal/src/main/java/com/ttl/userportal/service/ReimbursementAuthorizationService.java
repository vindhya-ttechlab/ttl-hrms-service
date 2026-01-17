package com.ttl.userportal.service;

import com.ttl.userportal.entity.Reimbursement;
import com.ttl.userportal.entity.Role;
import com.ttl.userportal.util.model.UserDetails;
import org.springframework.stereotype.Service;
@Service
public class ReimbursementAuthorizationService {

    public boolean isOwner(Reimbursement reimbursement, UserDetails userDetails) {
        if (reimbursement == null || userDetails == null || userDetails.getUser_id() == null) {
            return false;
        }
        Integer userId = userDetails.getUser_id().intValue();
        return userId.equals(reimbursement.getUserId());
    }

    public boolean isAssignedApprover(Reimbursement reimbursement, UserDetails userDetails) {
        if (reimbursement == null || userDetails == null || userDetails.getUser_id() == null) {
            return false;
        }
        Integer userId = userDetails.getUser_id().intValue();
        return userId.equals(reimbursement.getApproverId());
    }

    public boolean isHrApprover(Reimbursement reimbursement, UserDetails userDetails) {
        if (reimbursement == null || userDetails == null || userDetails.getUser_id() == null) {
            return false;
        }
        Integer userId = userDetails.getUser_id().intValue();
        return userId.equals(reimbursement.getHrApproverId());
    }

    public boolean hasHrRoleAccess(UserDetails userDetails) {
        if (userDetails == null || userDetails.getRole() == null) {
            return false;
        }

        for (Role role : userDetails.getRole()) {
            if (role == null || role.getRoleName() == null) continue;

            String roleName = role.getRoleName().trim().toLowerCase();
            switch (roleName) {
                case "hr":
                case "admin":
                case "superadmin":
                case "super_admin":
                    return true;
            }
        }
        return false;
    }

    public boolean canAccess(Reimbursement reimbursement, UserDetails userDetails) {
        if (reimbursement == null || userDetails == null || userDetails.getUser_id() == null) {
            return false;
        }

        // Ownership check
        if (isOwner(reimbursement, userDetails)) {
            return true;
        }

        // Approver check
        if (isAssignedApprover(reimbursement, userDetails)) {
            return true;
        }

        // HR approver check
        if (isHrApprover(reimbursement, userDetails)) {
            return true;
        }

        // Role-based HR access
        return hasHrRoleAccess(userDetails);
    }
    public boolean canApproveAsManager(Reimbursement reimbursement, UserDetails userDetails) {
        return isAssignedApprover(reimbursement, userDetails);
    }

    public boolean canApproveAsHr(Reimbursement reimbursement, UserDetails userDetails) {
        return hasHrRoleAccess(userDetails);
    }
}
