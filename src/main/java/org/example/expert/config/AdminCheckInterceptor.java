package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminCheckInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AdminCheckInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // 이 요청이 컨트롤러 메서드인지 확인함
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;

            String className = method.getBeanType().getName();
            String methodName = method.getMethod().getName();

            // 관리자만 쓰는 메서드인지 봄
            if (isAdminMethod(className, methodName)) {

                // 헤더에서 역할이랑 아이디 꺼내옴
                String role = request.getHeader("Role");
                String userId = request.getHeader("User-Id");

                // ADMIN 아니면 막음
                if (!"ADMIN".equalsIgnoreCase(role)) {
                    logger.warn("관리자 아님. 차단 - UserId={}, URL={}", userId, request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("관리자만 접근 가능");
                    return false;
                }

                // 관리자면 통과. 로그 남김
                logger.info("관리자 확인됨 - UserId={}, URL={}", userId, request.getRequestURI());
            }
        }

        // 나머진 그냥 통과
        return true;
    }

    // 이 메서드들이 관리자용인지 체크함
    private boolean isAdminMethod(String className, String methodName) {
        return ("org.example.expert.domain.comment.controller.CommentAdminController".equals(className)
                && "deleteComment".equals(methodName))
                || ("org.example.expert.domain.user.controller.UserAdminController".equals(className)
                && "changeUserRole".equals(methodName));
    }
}
