package com.example.user.utility;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.Assert;

public class Logout {
    protected final Log logger = LogFactory.getLog(this.getClass());
    private SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder
            .getContextHolderStrategy();
    private boolean invalidateHttpSession = true;
    private boolean clearAuthentication = true;
    private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Assert.notNull(request, "HttpServletRequest required");
        if (this.invalidateHttpSession) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug(LogMessage.format("Invalidated session %s", session.getId()));
                }
            }
        }

        SecurityContext context = this.securityContextHolderStrategy.getContext();
        this.securityContextHolderStrategy.clearContext();
        if (this.clearAuthentication) {
            context.setAuthentication(null);
        }

        SecurityContext emptyContext = this.securityContextHolderStrategy.createEmptyContext();
        this.securityContextRepository.saveContext(emptyContext, request, response);
    }
}
